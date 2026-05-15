package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleNotFoundExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "El rol especificado no existe en el sistema";

        RoleNotFoundException exception = new RoleNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable")
    void shouldBeThrowable() {
        assertThrows(RoleNotFoundException.class, () -> {
            throw new RoleNotFoundException("Role not found");
        });
    }
}
