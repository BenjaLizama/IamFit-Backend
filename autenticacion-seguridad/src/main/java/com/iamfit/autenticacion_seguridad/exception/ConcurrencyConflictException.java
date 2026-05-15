package com.iamfit.autenticacion_seguridad.exception;

public class ConcurrencyConflictException extends RuntimeException {
    public ConcurrencyConflictException(String message) {
        super(message);
    }
}
