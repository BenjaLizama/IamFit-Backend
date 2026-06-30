package com.iamfit.ai_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class InternalContextServiceTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    // Mocks para la cadena fluida de WebClient
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @Mock private Mono<Map> monoMap;
    @Mock private Mono<Object[]> monoArray;

    private InternalContextService internalContextService;

    private final String TOKEN = "mock-token";

    @BeforeEach
    void setUp() {
        // Configuramos el Builder para que retorne nuestro WebClient mockeado en el constructor del Service
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        internalContextService = new InternalContextService(
                webClientBuilder,
                "http://mock-alimentacion",
                "http://mock-ejercicios"
        );

        // Configuramos la estructura base de la cadena fluida (fluent API) compartida por todos los métodos
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(eq("Authorization"), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    // ==========================================
    // TESTS: getNutritionContext
    // ==========================================

    @Test
    @DisplayName("Debería retornar contexto nutricional manejando diferentes tipos numéricos (Integer, Double, String)")
    void getNutritionContext_Success() {
        // GIVEN
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("totalCalories", 2500);       // Prueba de Integer
        mockResponse.put("totalProtein", 150.5);       // Prueba de Double
        mockResponse.put("totalCarbohydrates", "200"); // Prueba de String parseable
        mockResponse.put("totalFat", null);            // Prueba de Null
        mockResponse.put("totalFiber", "error");       // Prueba de String no parseable

        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenReturn(mockResponse);

        // WHEN
        String result = internalContextService.getNutritionContext(TOKEN);

        // THEN
        assertNotNull(result);

        // Usamos String.format en las aserciones para que el test reconozca comas o puntos
        // dependiendo de si tu Mac está en Español o Inglés.
        assertTrue(result.contains(String.format("Calorias totales: %.0f kcal", 2500.0)));
        assertTrue(result.contains(String.format("Proteinas: %.1f g", 150.5)));
        assertTrue(result.contains(String.format("Carbohidratos: %.1f g", 200.0)));
        assertTrue(result.contains(String.format("Grasas: %.1f g", 0.0)));
        assertTrue(result.contains(String.format("Fibra: %.1f g", 0.0)));
    }

    @Test
    @DisplayName("Debería retornar string vacío si el WebClient lanza excepción al pedir nutrición")
    void getNutritionContext_Exception() {
        // GIVEN
        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenThrow(new RuntimeException("Connection refused"));

        // WHEN
        String result = internalContextService.getNutritionContext(TOKEN);

        // THEN
        assertEquals("", result);
    }

    // ==========================================
    // TESTS: getRoutinesContext
    // ==========================================

    @Test
    @DisplayName("Debería retornar las rutinas activas formateadas correctamente")
    void getRoutinesContext_Success() {
        // GIVEN
        Map<String, Object> rutina1 = Map.of(
                "name", "Fuerza Básica",
                "difficultyLevel", "Intermedio",
                "estimatedDurationMinutes", 45
        );
        Object[] mockRoutines = new Object[]{rutina1};

        when(responseSpec.bodyToMono(Object[].class)).thenReturn(monoArray);
        when(monoArray.block()).thenReturn(mockRoutines);

        // WHEN
        String result = internalContextService.getRoutinesContext(TOKEN);

        // THEN
        assertTrue(result.contains("Fuerza Básica"));
        assertTrue(result.contains("Intermedio"));
        assertTrue(result.contains("45 min"));
    }

    @Test
    @DisplayName("Debería retornar aviso si el usuario no tiene rutinas (Array vacío)")
    void getRoutinesContext_Empty() {
        // GIVEN
        when(responseSpec.bodyToMono(Object[].class)).thenReturn(monoArray);
        when(monoArray.block()).thenReturn(new Object[0]);

        // WHEN
        String result = internalContextService.getRoutinesContext(TOKEN);

        // THEN
        assertEquals("[RUTINAS] El usuario no tiene rutinas activas.\n", result);
    }

    @Test
    @DisplayName("Debería retornar string vacío si el WebClient lanza excepción al pedir rutinas")
    void getRoutinesContext_Exception() {
        // GIVEN
        when(responseSpec.bodyToMono(Object[].class)).thenReturn(monoArray);
        when(monoArray.block()).thenThrow(new RuntimeException("Timeout"));

        // WHEN
        String result = internalContextService.getRoutinesContext(TOKEN);

        // THEN
        assertEquals("", result);
    }

    // ==========================================
    // TESTS: getRoutineLimitsContext
    // ==========================================

    @Test
    @DisplayName("Debería retornar límites de rutinas indicando si puede crear más")
    void getRoutineLimitsContext_Success() {
        // GIVEN
        Map<String, Object> mockResponse = Map.of(
                "activeRoutines", 1,
                "maxActiveRoutines", 3,
                "canCreateRoutine", true
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenReturn(mockResponse);

        // WHEN
        String result = internalContextService.getRoutineLimitsContext(TOKEN);

        // THEN
        assertTrue(result.contains("Activas: 1/3"));
        assertTrue(result.contains("Puede crear: Sí"));
    }

    @Test
    @DisplayName("Debería retornar string vacío si el WebClient lanza excepción al pedir límites")
    void getRoutineLimitsContext_Exception() {
        // GIVEN
        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenThrow(new RuntimeException("Error"));

        // WHEN
        String result = internalContextService.getRoutineLimitsContext(TOKEN);

        // THEN
        assertEquals("", result);
    }

    // ==========================================
    // TESTS: getActiveMealPlanContext
    // ==========================================

    @Test
    @DisplayName("Debería retornar el plan de comidas activo")
    void getActiveMealPlanContext_Success() {
        // GIVEN
        Map<String, Object> mockResponse = Map.of(
                "title", "Dieta Keto",
                "goal", "Pérdida de grasa"
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenReturn(mockResponse);

        // WHEN
        String result = internalContextService.getActiveMealPlanContext(TOKEN);

        // THEN
        assertTrue(result.contains("Dieta Keto"));
        assertTrue(result.contains("Pérdida de grasa"));
    }

    @Test
    @DisplayName("Debería retornar aviso amistoso si el endpoint de comida devuelve 404 (Lanza Excepción)")
    void getActiveMealPlanContext_NotFound() {
        // GIVEN
        when(responseSpec.bodyToMono(Map.class)).thenReturn(monoMap);
        when(monoMap.block()).thenThrow(new RuntimeException("404 Not Found"));

        // WHEN
        String result = internalContextService.getActiveMealPlanContext(TOKEN);

        // THEN
        assertEquals("[PLAN DE COMIDAS] El usuario no tiene un plan activo.\n", result);
    }
}