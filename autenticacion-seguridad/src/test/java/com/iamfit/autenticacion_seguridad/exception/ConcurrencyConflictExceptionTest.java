package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConcurrencyConflictExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "La operación no pudo completarse porque otro proceso modificó los mismos datos.";

        ConcurrencyConflictException exception = new ConcurrencyConflictException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable y capturable")
    void shouldBeThrowable() {
        assertThrows(ConcurrencyConflictException.class, () -> {
            throw new ConcurrencyConflictException("Conflicto de concurrencia");
        });
    }
}