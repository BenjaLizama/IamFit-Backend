package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegisterWrapperTest {

    private UserProfileRequest mockProfile() {
        return new UserProfileRequest("testuser", 25, 70, 175, "M");
    }

    @Test
    @DisplayName("Debería crear RegisterWrapper correctamente con sus objetos anidados")
    void shouldCreateRegisterWrapper() {
        RegisterRequest registerRequest = new RegisterRequest("test@nomall.com", "SecurePass123!");
        SessionRequest sessionRequest = new SessionRequest("device-xyz", "Android Tablet");

        RegisterWrapper wrapper = new RegisterWrapper(registerRequest, mockProfile(), sessionRequest);

        assertNotNull(wrapper);
        assertEquals(registerRequest, wrapper.register());
        assertEquals(sessionRequest, wrapper.session());
        assertEquals("test@nomall.com", wrapper.register().email());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        RegisterRequest register = new RegisterRequest("user@mail.com", "Pass123!");
        SessionRequest session = new SessionRequest("id", "name");

        RegisterWrapper wrapper1 = new RegisterWrapper(register, mockProfile(), session);
        RegisterWrapper wrapper2 = new RegisterWrapper(register, mockProfile(), session);

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        RegisterRequest register = new RegisterRequest("user@mail.com", "Pass123!");
        SessionRequest session = new SessionRequest("id", "name");
        RegisterWrapper wrapper = new RegisterWrapper(register, mockProfile(), session);

        String result = wrapper.toString();

        assertNotNull(result);
        assertEquals(true, result.contains("register"));
        assertEquals(true, result.contains("session"));
    }
}