package com.iamfit.ejercicios.dto;

import lombok.Data;

@Data
public class EditRoutineExerciseRequest {
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer restSeconds;
    private String notes;
}