package com.iamfit.autenticacion_seguridad.exception;

public class UnsupportedAuthenticationProviderException extends RuntimeException {
    public UnsupportedAuthenticationProviderException(String message) {
        super(message);
    }
}
