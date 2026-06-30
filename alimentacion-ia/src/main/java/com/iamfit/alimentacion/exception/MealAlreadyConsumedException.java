package com.iamfit.alimentacion.exception;

public class MealAlreadyConsumedException extends RuntimeException {
    public MealAlreadyConsumedException(String message) { super(message); }
}