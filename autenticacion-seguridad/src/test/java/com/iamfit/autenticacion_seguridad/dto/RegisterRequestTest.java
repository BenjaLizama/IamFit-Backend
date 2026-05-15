package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegisterRequestTest {

    @Test
    @DisplayName("Debería crear RegisterRequest con datos válidos")
    void shouldCreateRegisterRequest() {
        String email = "usuario@ejemplo.com";
        String pass = "Password123!";

        RegisterRequest request = new RegisterRequest(email, pass);

        assertNotNull(request);
        assertEquals(email, request.email());
        assertEquals(pass, request.password());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        RegisterRequest request1 = new RegisterRequest("test@test.com", "Admin123#");
        RegisterRequest request2 = new RegisterRequest("test@test.com", "Admin123#");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Admin123#");
        String toString = request.toString();

        assertNotNull(toString);
        assertEquals(true, toString.contains("test@test.com"));
    }
}
