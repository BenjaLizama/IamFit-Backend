package com.iamfit.ejercicios.exception;

public class RoutineSessionExpiredException extends RuntimeException {
    public RoutineSessionExpiredException(String message) {
        super(message);
    }
}