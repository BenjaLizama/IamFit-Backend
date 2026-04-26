package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidPasswordExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "La contraseña actual es incorrecta.";

        InvalidPasswordException exception = new InvalidPasswordException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable y capturable")
    void shouldBeThrowable() {
        assertThrows(InvalidPasswordException.class, () -> {
            throw new InvalidPasswordException("Contraseña inválida");
        });
    }
}