package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.WeeklyMenuRequestDTO;
import com.iamfit.ai_service.dto.WeeklyMenuResponseDTO;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyMenuServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private UserProfileGrpcClient userProfileGrpcClient;

    @Mock
    private RateLimitConfig rateLimitConfig;

    @Mock
    private Bucket bucket;

    // Resuelve de forma limpia la API fluida: .defaultSystem().build().prompt().user().call().content()
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private WeeklyMenuService weeklyMenuService;

    private final String userId = "user-98765432-mock";

    @BeforeEach
    void setUp() {
        // Mockear el comportamiento del Rate Limiter por defecto (Permitir peticiones)
        lenient().when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        lenient().when(bucket.tryConsume(anyLong())).thenReturn(true);

        // Mockear el comportamiento de encadenamiento del ChatClient.Builder
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    @DisplayName("Debería generar un menú con éxito cuando el perfil existe y hay cuota en el rate limit")
    void generateMenu_Success() {
        // GIVEN
        WeeklyMenuRequestDTO request = new WeeklyMenuRequestDTO();
        request.setObjetivo("bajar");
        request.setCalorias(1800);
        request.setAlergias(List.of("Maní", "Lactosa"));

        UserProfileResponse mockProfile = UserProfileResponse.newBuilder()
                .setFound(true)
                .setWeight(85)
                .setHeight(180)
                .setAge(30)
                .setSex("M")
                .build();

        String menuSimulado = "Lunes: Desayuno: Avena... Martes: ... ⚠️ Importante: Este menú es una guía...";

        when(userProfileGrpcClient.getUserProfile(userId)).thenReturn(mockProfile);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(menuSimulado);

        // WHEN
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(userId, response.getUserId());
        assertEquals("bajar", response.getObjetivo());
        assertEquals(1800, response.getCalorias());
        assertEquals(menuSimulado, response.getMenu());
        assertNotNull(response.getDisclaimer());

        verify(userProfileGrpcClient, times(1)).getUserProfile(userId);
        verify(chatClient.prompt().user(anyString()).call(), times(1)).content();
    }

    @Test
    @DisplayName("Debería funcionar con éxito incluso si falla la obtención del perfil gRPC (Contexto vacío)")
    void generateMenu_SuccessWithoutProfile() {
        // GIVEN
        WeeklyMenuRequestDTO request = new WeeklyMenuRequestDTO();
        request.setObjetivo("subir");
        request.setCalorias(2500);
        request.setAlergias(null); // Sin alergias específicas

        // Simulamos que el cliente gRPC lanza una excepción
        when(userProfileGrpcClient.getUserProfile(userId)).thenThrow(new RuntimeException("gRPC Connection lost"));
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Menú generado sin datos físicos");

        // WHEN
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Menú generado sin datos físicos", response.getMenu());

        // Verifica que la excepción de gRPC fue capturada internamente y el proceso continuó
        verify(userProfileGrpcClient, times(1)).getUserProfile(userId);
    }

    @Test
    @DisplayName("Debería retornar estatus RATE_LIMITED cuando no queden tokens en el bucket")
    void generateMenu_RateLimited() {
        // GIVEN
        WeeklyMenuRequestDTO request = new WeeklyMenuRequestDTO();
        when(bucket.tryConsume(1)).thenReturn(false); // Bloquea el consumo

        // WHEN
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("RATE_LIMITED", response.getStatus());
        assertTrue(response.getMenu().contains("límite de consultas"));

        // Se asegura de que no se gastaron tokens consumiendo la IA de Vertex
        verifyNoInteractions(chatClientBuilder);
    }

    @Test
    @DisplayName("Debería retornar un DTO con estatus de ERROR controlado si la IA falla")
    void generateMenu_ExceptionHandled() {
        // GIVEN
        WeeklyMenuRequestDTO request = new WeeklyMenuRequestDTO();
        UserProfileResponse mockProfile = UserProfileResponse.newBuilder().setFound(false).build();

        when(userProfileGrpcClient.getUserProfile(userId)).thenReturn(mockProfile);
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("Vertex AI Quota Exceeded"));

        // WHEN
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(userId, request);

        // THEN
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertEquals("Error al generar el menú semanal.", response.getMenu());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Debería responder correctamente con el Fallback si el Circuit Breaker entra en acción")
    void generateMenuFallback_Triggered() {
        // GIVEN
        WeeklyMenuRequestDTO request = new WeeklyMenuRequestDTO();
        RuntimeException exception = new RuntimeException("Circuit open manually test");

        // WHEN
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenuFallback(userId, request, exception);

        // THEN
        assertNotNull(response);
        assertEquals("CIRCUIT_OPEN", response.getStatus());
        assertEquals(userId, response.getUserId());
        assertTrue(response.getMenu().contains("no está disponible"));
    }
}