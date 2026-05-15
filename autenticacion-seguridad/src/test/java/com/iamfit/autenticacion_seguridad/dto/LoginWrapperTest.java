package com.iamfit.autenticacion_seguridad.dto;

import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoginWrapperTest {

    @Test
    @DisplayName("Debería crear LoginWrapper correctamente con sus objetos anidados")
    void shouldCreateLoginWrapper() {
        // Preparar datos anidados
        LoginRequest login = new LoginRequest("user@test.com", "Pass123!", LoginProvider.LOCAL);
        SessionRequest session = new SessionRequest("device-id-123", "Chrome/Windows");

        // Instanciar Wrapper
        LoginWrapper wrapper = new LoginWrapper(login, session);

        // Verificaciones
        assertNotNull(wrapper);
        assertEquals(login, wrapper.login());
        assertEquals(session, wrapper.session());

        // Verificar integridad de datos internos
        assertEquals("user@test.com", wrapper.login().identifier());
        assertEquals("device-id-123", wrapper.session().deviceId());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        LoginRequest login = new LoginRequest("a", "b", LoginProvider.LOCAL);
        SessionRequest session = new SessionRequest("c", "d");

        LoginWrapper wrapper1 = new LoginWrapper(login, session);
        LoginWrapper wrapper2 = new LoginWrapper(login, session);

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        LoginRequest login = new LoginRequest("user", "pass", LoginProvider.LOCAL);
        SessionRequest session = new SessionRequest("id", "name");
        LoginWrapper wrapper = new LoginWrapper(login, session);

        String result = wrapper.toString();

        assertNotNull(result);
        assertEquals(true, result.contains("login"));
        assertEquals(true, result.contains("session"));
    }
}