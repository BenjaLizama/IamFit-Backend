package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import lombok.Data;

@Data
public class UpdateRoutineRequest {
    private String name;
    private String description;
    private DifficultyLevel difficultyLevel;
    private Integer estimatedDurationMinutes;
}