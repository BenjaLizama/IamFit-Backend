package com.iamfit.ejercicios.exception;

public class ExerciseAlreadyCompletedException extends RuntimeException {
    public ExerciseAlreadyCompletedException(String message) { super(message); }
}