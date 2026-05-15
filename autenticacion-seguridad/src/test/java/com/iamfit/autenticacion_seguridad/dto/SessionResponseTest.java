package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessionResponseTest {

    @Test
    @DisplayName("Debería crear SessionResponse correctamente")
    void shouldCreateSessionResponse() {
        String token = "raw-token-xyz";
        Instant expiry = Instant.now().plusSeconds(3600);
        String device = "iPhone 15 Pro";

        SessionResponse response = new SessionResponse(token, expiry, device);

        assertNotNull(response);
        assertEquals(token, response.rawRefreshToken());
        assertEquals(expiry, response.expiryDate());
        assertEquals(device, response.deviceName());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        Instant now = Instant.parse("2026-04-21T10:00:00Z");
        SessionResponse response1 = new SessionResponse("tk", now, "pc");
        SessionResponse response2 = new SessionResponse("tk", now, "pc");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        SessionResponse response = new SessionResponse("token", Instant.MIN, "device");
        String result = response.toString();

        assertNotNull(result);
        assertEquals(true, result.contains("token"));
        assertEquals(true, result.contains("device"));
    }
}
