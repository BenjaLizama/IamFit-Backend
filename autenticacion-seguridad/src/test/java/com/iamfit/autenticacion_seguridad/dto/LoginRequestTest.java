package com.iamfit.autenticacion_seguridad.dto;

import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginRequestTest {

    @Test
    @DisplayName("Debería crear LoginRequest correctamente con todos los campos")
    void shouldCreateLoginRequest() {
        String identifier = "test@example.com";
        String password = "password123";
        LoginProvider provider = LoginProvider.LOCAL;

        LoginRequest request = new LoginRequest(identifier, password, provider);

        assertNotNull(request);
        assertEquals(identifier, request.identifier());
        assertEquals(password, request.password());
        assertEquals(provider, request.provider());
    }

    @Test
    @DisplayName("Debería permitir password nulo para proveedores externos")
    void shouldAllowNullPasswordForExternalProviders() {
        LoginRequest request = new LoginRequest("google-token", null, LoginProvider.GOOGLE);

        assertEquals("google-token", request.identifier());
        assertNull(request.password());
        assertEquals(LoginProvider.GOOGLE, request.provider());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        LoginRequest request1 = new LoginRequest("user", "pass", LoginProvider.LOCAL);
        LoginRequest request2 = new LoginRequest("user", "pass", LoginProvider.LOCAL);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}
