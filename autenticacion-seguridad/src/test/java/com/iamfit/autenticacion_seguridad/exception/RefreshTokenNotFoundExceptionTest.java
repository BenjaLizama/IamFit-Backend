package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshTokenNotFoundExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "Refresh token no encontrado o expirado";

        RefreshTokenNotFoundException exception = new RefreshTokenNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable")
    void shouldBeThrowable() {
        assertThrows(RefreshTokenNotFoundException.class, () -> {
            throw new RefreshTokenNotFoundException("Token error");
        });
    }
}
