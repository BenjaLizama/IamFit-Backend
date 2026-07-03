package com.iamfit.ejercicios.dto;

public record CompleteSessionExerciseRequest(
        Integer setsCompleted, Integer repsCompleted, Double weightUsed, String notes) {}