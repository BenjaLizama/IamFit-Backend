package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeactivateResponseTest {

    @Test
    @DisplayName("Debería crear DeactivateResponse con datos válidos")
    void shouldCreateDeactivateResponse() {
        String message = "Cuenta desactivada con éxito.";
        Instant now = Instant.now();

        DeactivateResponse response = new DeactivateResponse(message, now);

        assertNotNull(response);
        assertEquals(message, response.message());
        assertEquals(now, response.deactivatedAt());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        String message = "Éxito";
        Instant time = Instant.parse("2026-04-24T10:00:00Z");

        DeactivateResponse response1 = new DeactivateResponse(message, time);
        DeactivateResponse response2 = new DeactivateResponse(message, time);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        DeactivateResponse response = new DeactivateResponse("Mensaje de prueba", Instant.now());
        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Mensaje de prueba"));
        assertTrue(toString.contains("deactivatedAt"));
    }
}
