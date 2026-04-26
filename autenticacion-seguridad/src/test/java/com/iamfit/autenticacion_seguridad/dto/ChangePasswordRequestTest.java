package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChangePasswordRequestTest {

    @Test
    @DisplayName("Debería crear ChangePasswordRequest con datos válidos")
    void shouldCreateChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");

        assertNotNull(request);
        assertEquals("OldPass123!", request.oldPassword());
        assertEquals("NewPass123!", request.newPassword());
    }

    @Test
    @DisplayName("Prueba de equals y hashCode")
    void testEqualsAndHashCode() {
        ChangePasswordRequest request1 = new ChangePasswordRequest("OldPass123!", "NewPass123!");
        ChangePasswordRequest request2 = new ChangePasswordRequest("OldPass123!", "NewPass123!");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Prueba de toString")
    void testToString() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");
        String toString = request.toString();

        assertNotNull(toString);
        assertEquals(true, toString.contains("OldPass123!"));
    }
}
