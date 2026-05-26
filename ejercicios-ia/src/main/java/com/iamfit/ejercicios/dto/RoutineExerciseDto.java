package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class RoutineExerciseDto {
    private UUID id;
    private UUID exerciseId;
    private String exerciseName;
    private MuscleGroup muscleGroup;
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;

}