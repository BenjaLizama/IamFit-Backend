package com.iamfit.ejercicios.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test-uri");
    }

    @Test
    @DisplayName("Debería manejar RoutineSessionExpiredException con status 410")
    void handleSessionExpired() {
        // Given
        RoutineSessionExpiredException ex = new RoutineSessionExpiredException("Sesión expirada");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleSessionExpired(ex, request);

        // Then
        assertEquals(410, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(410, body.get("status"));
        assertEquals("ROUTINE_SESSION_EXPIRED", body.get("code"));
        assertEquals("Sesión expirada", body.get("message"));
        assertEquals("/api/v1/test-uri", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Debería manejar RoutineLimitReachedException con status 409")
    void handleLimitReached() {
        // Given
        RoutineLimitReachedException ex = new RoutineLimitReachedException("Límite alcanzado");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleLimitReached(ex, request);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("ROUTINE_LIMIT_REACHED", body.get("code"));
        assertEquals("Límite alcanzado", body.get("message"));
    }

    @Test
    @DisplayName("Debería manejar IllegalArgumentException con status 400")
    void handleIllegalArgument() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("VALIDATION_ERROR", body.get("code"));
        assertEquals("Argumento inválido", body.get("message"));
    }

    @Test
    @DisplayName("Debería manejar MethodArgumentNotValidException con status 400 y mapear errores de campos")
    void handleValidation() {
        // Given: Mockeamos la excepción de validación de Spring y su BindingResult
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulamos un error en el campo "difficulty"
        FieldError fieldError = new FieldError("generateRoutineRequest", "difficulty", "La dificultad es obligatoria");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidation(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("ERR_VALIDATION_001", body.get("code"));
        assertEquals("Por favor verifica los datos ingresados.", body.get("message"));

        // Verificamos que el mapa de "fields" se haya llenado correctamente
        assertTrue(body.containsKey("fields"));
        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) body.get("fields");
        assertEquals("La dificultad es obligatoria", fields.get("difficulty"));
    }

    @Test
    @DisplayName("Debería manejar Exception (Error genérico) con status 500")
    void handleGeneric() {
        // Given
        Exception ex = new Exception("Error catastrófico en base de datos");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("SYS_500", body.get("code"));
        // Comprobamos que oculta el mensaje real por seguridad y muestra el genérico
        assertEquals("Error interno del servidor.", body.get("message"));
    }
}