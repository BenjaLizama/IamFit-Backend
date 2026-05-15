package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RefreshTokenWrapperTest {

    @Test
    @DisplayName("Debería crear RefreshTokenWrapper correctamente con sus objetos anidados")
    void shouldCreateRefreshTokenWrapper() {
        // Preparar datos
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest("refresh-token-123");
        SessionRequest sessionRequest = new SessionRequest("dev-id", "Linux Desktop");

        // Instanciar
        RefreshTokenWrapper wrapper = new RefreshTokenWrapper(tokenRequest, sessionRequest);

        // Verificaciones
        assertNotNull(wrapper);
        assertEquals(tokenRequest, wrapper.token());
        assertEquals(sessionRequest, wrapper.session());
        assertEquals("refresh-token-123", wrapper.token().requestToken());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        RefreshTokenRequest token = new RefreshTokenRequest("tk");
        SessionRequest session = new SessionRequest("id", "name");

        RefreshTokenWrapper wrapper1 = new RefreshTokenWrapper(token, session);
        RefreshTokenWrapper wrapper2 = new RefreshTokenWrapper(token, session);

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        RefreshTokenRequest token = new RefreshTokenRequest("tk");
        SessionRequest session = new SessionRequest("id", "name");
        RefreshTokenWrapper wrapper = new RefreshTokenWrapper(token, session);

        String result = wrapper.toString();

        assertNotNull(result);
        assertEquals(true, result.contains("token"));
        assertEquals(true, result.contains("session"));
    }
}
