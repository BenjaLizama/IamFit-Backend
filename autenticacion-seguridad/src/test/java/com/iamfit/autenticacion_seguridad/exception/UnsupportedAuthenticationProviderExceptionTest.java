package com.iamfit.autenticacion_seguridad.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnsupportedAuthenticationProviderExceptionTest {

    @Test
    @DisplayName("Debería instanciar la excepción con el mensaje correcto")
    void shouldCreateExceptionWithMessage() {
        String message = "Proveedor de autenticación no soportado";

        UnsupportedAuthenticationProviderException exception = new UnsupportedAuthenticationProviderException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debería ser lanzable")
    void shouldBeThrowable() {
        assertThrows(UnsupportedAuthenticationProviderException.class, () -> {
            throw new UnsupportedAuthenticationProviderException("Unsupported provider");
        });
    }
}
