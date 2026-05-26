package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RoutineDto {
    private UUID id;
    private String name;
    private String description;
    private DifficultyLevel difficultyLevel;
    private Integer estimatedDurationMinutes;
    private Boolean aiGenerated;
    private LocalDateTime createdAt;
    private List<RoutineExerciseDto> exercises;
}