package com.iamfit.ai_service.service;

import com.iamfit.ai_service.dto.FeedbackRequestDTO;
import com.iamfit.ai_service.dto.FeedbackResponseDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    // Simula de forma limpia la cadena fluida: .prompt().user().call().content()
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private FeedbackService feedbackService;

    private final String userId = "user-feedback-123";

    @BeforeEach
    void setUp() {
        // Configuramos el comportamiento base del builder del ChatClient
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    @DisplayName("Debería generar feedback con éxito cuando el usuario bajó de peso y tiene ejercicios")
    void generateFeedback_Success_WeightLoss() {
        // GIVEN
        FeedbackRequestDTO request = new FeedbackRequestDTO();
        request.setUserId(userId);
        request.setObjetivo("bajar");
        request.setPesoInicialSemana(75.0);
        request.setPesoActual(73.5); // Bajó 1.5 kg
        request.setCaloriasConsumidas(1800);
        request.setAguaConsumida(2.5);
        request.setHorasSueno(7.5);
        request.setEjerciciosRealizados(List.of("Sentadillas", "Cardio"));
        request.setPromedioCaloriasSemana(1900);
        request.setPromedioAguaSemana(2.0);
        request.setPromedioSuenoSemana(7.0);
        request.setDiasEntrenadosSemana(4);

        String iaResponse = "¡Excelente trabajo! Has bajado de peso y tus ejercicios están dando resultados.";
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(iaResponse);

        // WHEN
        FeedbackResponseDTO response = feedbackService.generateFeedback(request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(userId, response.getUserId());
        assertEquals(iaResponse, response.getFeedback());

        verify(chatClient.prompt().user(anyString()).call(), times(1)).content();
    }

    @Test
    @DisplayName("Debería generar feedback con éxito cuando el usuario subió de peso y no tiene ejercicios (Lista vacía)")
    void generateFeedback_Success_WeightGain_EmptyExercises() {
        // GIVEN
        FeedbackRequestDTO request = new FeedbackRequestDTO();
        request.setUserId(userId);
        request.setObjetivo("subir");
        request.setPesoInicialSemana(60.0);
        request.setPesoActual(62.0); // Subió 2.0 kg
        request.setEjerciciosRealizados(List.of()); // Lista vacía

        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Vas muy bien con tu objetivo de subir de peso.");

        // WHEN
        FeedbackResponseDTO response = feedbackService.generateFeedback(request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Vas muy bien con tu objetivo de subir de peso.", response.getFeedback());
    }

    @Test
    @DisplayName("Debería generar feedback con éxito cuando el peso se mantuvo igual y la lista de ejercicios es null")
    void generateFeedback_Success_WeightMaintained_NullExercises() {
        // GIVEN
        FeedbackRequestDTO request = new FeedbackRequestDTO();
        request.setUserId(userId);
        request.setObjetivo("mantener");
        request.setPesoInicialSemana(70.0);
        request.setPesoActual(70.0); // Se mantuvo
        request.setEjerciciosRealizados(null); // Lista nula

        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Perfecto, lograste mantener tu peso ideal esta semana.");

        // WHEN
        FeedbackResponseDTO response = feedbackService.generateFeedback(request);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Perfecto, lograste mantener tu peso ideal esta semana.", response.getFeedback());
    }

    @Test
    @DisplayName("Debería retornar un DTO de ERROR si el ChatClient lanza una excepción")
    void generateFeedback_ExceptionHandled() {
        // GIVEN
        FeedbackRequestDTO request = new FeedbackRequestDTO();
        request.setUserId(userId);

        // Simulamos que la IA falla (ej. timeout de red)
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("Error interno de Gemini"));

        // WHEN
        FeedbackResponseDTO response = feedbackService.generateFeedback(request);

        // THEN
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertEquals("Error al generar el feedback.", response.getFeedback());
        assertEquals(userId, response.getUserId());
    }
}