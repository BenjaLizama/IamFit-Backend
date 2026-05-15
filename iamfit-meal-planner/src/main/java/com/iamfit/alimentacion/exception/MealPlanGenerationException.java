package com.iamfit.alimentacion.exception;

public class MealPlanGenerationException extends RuntimeException {

    public MealPlanGenerationException(String message) {
        super(message);
    }

    public MealPlanGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
