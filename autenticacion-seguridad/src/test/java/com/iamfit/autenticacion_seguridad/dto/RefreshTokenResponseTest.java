package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RefreshTokenResponseTest {

    @Test
    @DisplayName("Debería crear RefreshTokenResponse correctamente")
    void shouldCreateRefreshTokenResponse() {
        String token = "new-access-token-789";
        Instant expiry = Instant.now().plusSeconds(3600);

        RefreshTokenResponse response = new RefreshTokenResponse(token, expiry);

        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(expiry, response.expiryDate());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        Instant expiry = Instant.parse("2026-01-01T10:00:00Z");
        RefreshTokenResponse response1 = new RefreshTokenResponse("tk", expiry);
        RefreshTokenResponse response2 = new RefreshTokenResponse("tk", expiry);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}
