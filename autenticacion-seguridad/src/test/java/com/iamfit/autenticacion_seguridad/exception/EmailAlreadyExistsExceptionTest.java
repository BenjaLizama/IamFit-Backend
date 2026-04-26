package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "El correo electrónico ya está registrado";

        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable")
    void shouldBeThrowable() {
        assertThrows(EmailAlreadyExistsException.class, () -> {
            throw new EmailAlreadyExistsException("Test message");
        });
    }
}
