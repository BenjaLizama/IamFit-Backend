package com.iamfit.ejercicios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.UUID;

@Data
public class AddExerciseToRoutineRequest {

    @NotNull(message = "El ejercicio es obligatorio")
    private UUID exerciseId;

    @NotNull @Positive
    private Integer sets;

    @NotNull @Positive
    private Integer reps;

    private Double weightKg;

    private Integer restSeconds;

    private String notes;
}