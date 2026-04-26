package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialNotFoundExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "Credenciales no encontradas en la base de datos";

        CredentialNotFoundException exception = new CredentialNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable y capturable")
    void shouldBeThrowable() {
        String message = "Error de credencial";

        assertThrows(CredentialNotFoundException.class, () -> {
            throw new CredentialNotFoundException(message);
        });
    }
}
