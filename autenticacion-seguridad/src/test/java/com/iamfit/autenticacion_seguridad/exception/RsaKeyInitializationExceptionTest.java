package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RsaKeyInitializationExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con mensaje y causa")
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Error al inicializar llaves RSA";
        Throwable cause = new RuntimeException("Causa original");

        RsaKeyInitializationException exception = new RsaKeyInitializationException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Debería ser lanzable")
    void shouldBeThrowable() {
        assertThrows(RsaKeyInitializationException.class, () -> {
            throw new RsaKeyInitializationException("Error", new RuntimeException());
        });
    }
}
