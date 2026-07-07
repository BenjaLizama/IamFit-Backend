package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.MotivationRequestDTO;
import com.iamfit.ai_service.dto.TechniqueRequestDTO;
import com.iamfit.ai_service.dto.WellbeingCheckInRequestDTO;
import com.iamfit.ai_service.dto.WellbeingResponseDTO;
import com.iamfit.grpc.common.UserProfileResponse;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WellbeingServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private UserProfileGrpcClient userProfileGrpcClient;

    @Mock
    private RateLimitConfig rateLimitConfig;

    @Mock
    private Bucket bucket;

    // Usamos Answers.RETURNS_DEEP_STUBS para mockear la API fluida del ChatClient sin escribir toneladas de código
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private WellbeingService wellbeingService;

    private final String userId = "user-12345678-mock";

    @BeforeEach
    void setUp() {
        // Configuración común para el simulador de Rate Limit (por defecto permite consumir)
        lenient().when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        lenient().when(bucket.tryConsume(anyLong())).thenReturn(true);

        // Configuración común para simular el comportamiento fluido de ChatClient
        // chatClientBuilder.defaultSystem(...).build().prompt().user(...).call().content()
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    @DisplayName("Debería procesar CheckIn con éxito cuando hay perfil gRPC y cuota disponible")
    void checkIn_Success() {
        // GIVEN
        WellbeingCheckInRequestDTO request = new WellbeingCheckInRequestDTO();
        request.setEstadoAnimo(4); // Bien
        request.setNivelEstres(2); // Poco estresado
        request.setNota("Un poco cansado pero motivado");

        UserProfileResponse mockProfile = UserProfileResponse.newBuilder()
                .setFound(true)
                .setAge(25)
                .setWeight(70)
                .setHeight(175)
                .setSex("M")
                .build();

        when(userProfileGrpcClient.getUserProfile(userId)).thenReturn(mockProfile);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Respuesta simulada de MIA para el Check-In");

        // WHEN
        WellbeingResponseDTO response = wellbeingService.checkIn(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("CHECK_IN", response.getTipo());
        assertEquals("Respuesta simulada de MIA para el Check-In", response.getResponse());
        assertNotNull(response.getDisclaimer());

        verify(userProfileGrpcClient, times(1)).getUserProfile(userId);
        verify(chatClient.prompt().user(anyString()).call(), times(1)).content();
    }

    @Test
    @DisplayName("Debería rechazar por Rate Limit si el bucket está vacío")
    void checkIn_RateLimited() {
        // GIVEN
        WellbeingCheckInRequestDTO request = new WellbeingCheckInRequestDTO();
        when(bucket.tryConsume(1)).thenReturn(false); // Bloquea la petición

        // WHEN
        WellbeingResponseDTO response = wellbeingService.checkIn(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("RATE_LIMITED", response.getStatus());
        assertTrue(response.getResponse().contains("límite de consultas"));

        // Verifica que no se llamó a la IA por falta de tokens
        verifyNoInteractions(chatClientBuilder);
    }

    @Test
    @DisplayName("Debería generar Motivación con éxito interpretando el contexto del request")
    void getMotivation_Success() {
        // GIVEN
        MotivationRequestDTO request = new MotivationRequestDTO();
        request.setContexto("antes de entrenar pierna");

        UserProfileResponse mockProfile = UserProfileResponse.newBuilder().setFound(false).build();
        when(userProfileGrpcClient.getUserProfile(userId)).thenReturn(mockProfile);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("¡Vamos! A romperla en esa sentadilla.");

        // WHEN
        WellbeingResponseDTO response = wellbeingService.getMotivation(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("MOTIVATION", response.getTipo());
        assertEquals("¡Vamos! A romperla en esa sentadilla.", response.getResponse());
    }

    @Test
    @DisplayName("Debería generar una Técnica de bienestar correctamente")
    void getTechnique_Success() {
        // GIVEN
        TechniqueRequestDTO request = new TechniqueRequestDTO();
        request.setTipo("respiracion");

        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Paso 1: Inhala en 4 segundos...");

        // WHEN
        WellbeingResponseDTO response = wellbeingService.getTechnique(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("TECHNIQUE", response.getTipo());
        assertTrue(response.getResponse().contains("Inhala"));
    }

    @Test
    @DisplayName("Debería retornar DTO de Error controlado si el ChatClient lanza una excepción")
    void checkIn_ExceptionHandled() {
        // GIVEN
        WellbeingCheckInRequestDTO request = new WellbeingCheckInRequestDTO();
        request.setEstadoAnimo(3);
        request.setNivelEstres(3);

        when(chatClient.prompt().user(anyString()).call().content()).thenThrow(new RuntimeException("Vertex AI Timeout"));

        // WHEN
        WellbeingResponseDTO response = wellbeingService.checkIn(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertEquals("Error al procesar tu check-in.", response.getResponse());
    }

    @Test
    @DisplayName("Debería activarse el Fallback del Circuit Breaker directamente ante fallos estructurales")
    void wellbeingFallback_Triggered() {
        // GIVEN
        RuntimeException exception = new RuntimeException("Circuit open test");

        // WHEN
        WellbeingResponseDTO response = wellbeingService.wellbeingFallback(userId, new Object(), exception);

        // THEN
        assertNotNull(response);
        assertEquals("CIRCUIT_OPEN", response.getStatus());
        assertEquals("UNAVAILABLE", response.getTipo());
        assertTrue(response.getResponse().contains("no está disponible en este momento"));
    }
}