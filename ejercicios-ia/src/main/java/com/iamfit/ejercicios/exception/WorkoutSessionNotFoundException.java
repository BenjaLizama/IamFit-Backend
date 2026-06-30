package com.iamfit.ejercicios.exception;

public class WorkoutSessionNotFoundException extends RuntimeException {
    public WorkoutSessionNotFoundException(String message) { super(message); }
}