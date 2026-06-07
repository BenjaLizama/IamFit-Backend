package com.iamfit.ejercicios.exception;

public class RoutineLimitReachedException extends RuntimeException {
    public RoutineLimitReachedException(String message) {
        super(message);
    }
}