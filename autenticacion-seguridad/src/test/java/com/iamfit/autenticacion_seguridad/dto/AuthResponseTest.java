package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthResponseTest {

    @Test
    @DisplayName("Debería crear la respuesta con los datos correctos")
    void shouldCreateAuthResponse() {
        String accessToken = "access-123";
        String refreshToken = "refresh-456";
        Instant now = Instant.now();

        AuthResponse response = new AuthResponse(accessToken, refreshToken, now);

        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
        assertEquals(now, response.expiresAt());
    }

    @Test
    @DisplayName("Prueba de métodos equals y hashCode")
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        AuthResponse response1 = new AuthResponse("a", "r", now);
        AuthResponse response2 = new AuthResponse("a", "r", now);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}