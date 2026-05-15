package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RefreshTokenRequestTest {

    @Test
    @DisplayName("Debería crear RefreshTokenRequest con los datos correctos")
    void shouldCreateRefreshTokenRequest() {
        String token = "some-refresh-token-123";

        RefreshTokenRequest request = new RefreshTokenRequest(token);

        assertNotNull(request);
        assertEquals(token, request.requestToken());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        RefreshTokenRequest request1 = new RefreshTokenRequest("token123");
        RefreshTokenRequest request2 = new RefreshTokenRequest("token123");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        RefreshTokenRequest request = new RefreshTokenRequest("token123");
        String toString = request.toString();

        assertNotNull(toString);
        assertEquals(true, toString.contains("token123"));
    }
}
