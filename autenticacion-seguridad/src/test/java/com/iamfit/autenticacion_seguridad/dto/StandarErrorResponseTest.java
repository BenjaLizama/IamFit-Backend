package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandarErrorResponseTest {

    @Test
    @DisplayName("Debería crear StandarErrorResponse usando el Builder de Lombok")
    void shouldCreateResponseWithBuilder() {
        Map<String, String> errors = Map.of("email", "Formato inválido");
        long now = System.currentTimeMillis();

        StandarErrorResponse response = StandarErrorResponse.builder()
                .status(400)
                .code("VALIDATION_ERROR")
                .error("Bad Request")
                .message("Error en los datos")
                .developerMessage("Constraint violation on email field")
                .path("/api/auth/register")
                .timestamp(now)
                .validationError(errors)
                .build();

        assertNotNull(response);
        assertEquals(400, response.status());
        assertEquals("VALIDATION_ERROR", response.code());
        assertEquals("Bad Request", response.error());
        assertEquals("Error en los datos", response.message());
        assertEquals("Constraint violation on email field", response.developerMessage());
        assertEquals("/api/auth/register", response.path());
        assertEquals(now, response.timestamp());
        assertEquals(errors, response.validationError());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        StandarErrorResponse res1 = StandarErrorResponse.builder().status(404).build();
        StandarErrorResponse res2 = StandarErrorResponse.builder().status(404).build();

        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        StandarErrorResponse response = StandarErrorResponse.builder().code("ERR_01").build();
        String result = response.toString();

        assertNotNull(result);
        assertTrue(result.contains("ERR_01"));
    }
}
