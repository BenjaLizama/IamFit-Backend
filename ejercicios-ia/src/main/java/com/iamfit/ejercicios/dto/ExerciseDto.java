package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ExerciseDto {
    private UUID id;
    private String name;
    private String description;
    private MuscleGroup muscleGroup;
    private Equipment equipment;
    private DifficultyLevel difficulty;
    private Integer defaultSets;
    private Integer defaultReps;
    private Integer defaultRestSeconds;
    private String videoUrl;
}