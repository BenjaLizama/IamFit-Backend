package com.iamfit.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.dto.AIResponseDTO;
import com.iamfit.grpc.common.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @InjectMocks
    private AIService aiService;

    @Mock private UserProfileGrpcClient userProfileGrpcClient;
    @Mock private InternalContextService internalContextService;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;

    // Mocks explícitos para romper la fragilidad del Deep Stub fluido
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;

    private final String TEST_USER_ID = "USER_123456";
    private final String TEST_TOKEN = "Bearer token-valido";



    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "model", "gemini-1.5-flash");

        // 1. Mockeamos defaultSystem pasándole cualquier String
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);

        // 2. 👇 LA CLAVE: Mockear la firma exacta Varargs (Array de Advisors) que usa tu código real
        lenient().when(chatClientBuilder.defaultAdvisors(any(Advisor[].class))).thenReturn(chatClientBuilder);

        // 3. Cerramos la cadena del Builder retornando el mock del cliente
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);

        // Configuración por defecto para contextos internos
        lenient().when(internalContextService.getNutritionContext(anyString())).thenReturn("");
        lenient().when(internalContextService.getRoutinesContext(anyString())).thenReturn("");
        lenient().when(internalContextService.getRoutineLimitsContext(anyString())).thenReturn("");
        lenient().when(internalContextService.getActiveMealPlanContext(anyString())).thenReturn("");

        // Flujo base del Prompt estable para las peticiones
        lenient().when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.advisors(any(Consumer.class))).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    @DisplayName("chat() - Debe retornar respuesta exitosa y estructurada cuando el perfil y la IA responden correctamente")
    void chat_SuccessStructuredResponse() {
        // Arrange
        UserProfileResponse mockProfile = UserProfileResponse.newBuilder()
                .setFound(true)
                .setNickname("Carlos")
                .setAge(28)
                .setWeight(75)
                .setHeight(175)
                .setSex("MASCULINO")
                .build();

        when(userProfileGrpcClient.getUserProfile(TEST_USER_ID)).thenReturn(mockProfile);
        when(internalContextService.getNutritionContext(TEST_TOKEN)).thenReturn("- Consumo diario: 2000 kcal\n");
        when(internalContextService.getRoutinesContext(TEST_TOKEN)).thenReturn("- Rutina actual: Hipertrofia\n");

        String jsonIaResponse = """
                {
                  "content": "Hola Carlos, veo que vas excelente con tus 2000 kcal.",
                  "actions": [
                    {
                      "type": "GET_MOTIVATION",
                      "label": "Seguir motivado",
                      "payload": { "contexto": "entrenamiento" }
                    }
                  ]
                }
                """;

        when(callResponseSpec.content()).thenReturn(jsonIaResponse);

        // Act
        AIResponseDTO response = aiService.chat(TEST_USER_ID, "Dame consejos para hoy", TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("gemini-1.5-flash", response.getModel());
        assertTrue(response.getContent().contains("Hola Carlos"));
        assertFalse(response.getActions().isEmpty());
        assertEquals("GET_MOTIVATION", response.getActions().get(0).getType());
    }

    @Test
    @DisplayName("chat() - Debe retornar texto plano sanitizado si la IA rompe el formato JSON esperado")
    void chat_FallbackToPlainTextOnJsonError() {
        // Arrange
        UserProfileResponse mockProfile = UserProfileResponse.newBuilder().setFound(true).setNickname("Ana").build();
        when(userProfileGrpcClient.getUserProfile(TEST_USER_ID)).thenReturn(mockProfile);

        String invalidJsonResponse = "Claro Ana, entrena duro hoy.";
        when(callResponseSpec.content()).thenReturn(invalidJsonResponse);

        // Act
        AIResponseDTO response = aiService.chat(TEST_USER_ID, "Hola", TEST_TOKEN);

        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Claro Ana, entrena duro hoy.", response.getContent());
        assertTrue(response.getActions().isEmpty());
    }

    @Test
    @DisplayName("chat() - Debe solicitar completar perfil si gRPC indica que el usuario no existe")
    void chat_IncompleteProfile() {
        // Arrange
        UserProfileResponse profileNotFound = UserProfileResponse.newBuilder()
                .setFound(false)
                .build();

        when(userProfileGrpcClient.getUserProfile(TEST_USER_ID)).thenReturn(profileNotFound);

        // Act
        AIResponseDTO response = aiService.chat(TEST_USER_ID, "Hola", TEST_TOKEN);

        // Assert
        assertEquals("PROFILE_INCOMPLETE", response.getStatus());
        assertTrue(response.getContent().contains("completar tu perfil primero"));

        // Corregido: Verificamos que no se intentó crear un chatClient nuevo después de ver el perfil incompleto
        verify(chatClientBuilder, times(0)).build();
    }

    @Test
    @DisplayName("chat() - Debe interceptar mensajes restrictivos en inglés ('I cannot') y devolver aviso amigable")
    void chat_ValidatesOutputAndSanitizesLanguages() {
        // Arrange
        UserProfileResponse mockProfile = UserProfileResponse.newBuilder().setFound(true).setNickname("User").build();
        when(userProfileGrpcClient.getUserProfile(TEST_USER_ID)).thenReturn(mockProfile);

        String restrictedIaResponse = "I am sorry, I cannot perform this action.";
        when(callResponseSpec.content()).thenReturn(restrictedIaResponse);

        // Act
        AIResponseDTO response = aiService.chat(TEST_USER_ID, "Haz algo ilegal", TEST_TOKEN);

        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("No puedo responder esa consulta. ¿Tienes alguna pregunta sobre salud o fitness?", response.getContent());
    }

    @Test
    @DisplayName("chatFallback() - Debe activarse correctamente devolviendo estado CIRCUIT_OPEN")
    void chatFallback_ReturnsCircuitOpenResponse() {
        // Arrange
        Exception exceptionSimulada = new RuntimeException("Vertex AI sobrecargado");

        // Act
        AIResponseDTO response = aiService.chatFallback(TEST_USER_ID, "Hola", TEST_TOKEN, exceptionSimulada);

        // Assert
        assertNotNull(response);
        assertEquals("CIRCUIT_OPEN", response.getStatus());
        assertEquals("unavailable", response.getModel());
        assertTrue(response.getContent().contains("M.I.A. no esta disponible en este momento"));
    }
}