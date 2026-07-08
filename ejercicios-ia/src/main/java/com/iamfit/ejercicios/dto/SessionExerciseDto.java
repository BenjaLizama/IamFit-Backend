package com.iamfit.ejercicios.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SessionExerciseDto {
    private UUID exerciseEntryId;
    private String exerciseName;
    private String muscleGroup;
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer restSeconds;
    private Integer orderIndex;
    private Boolean completed;
    private Integer setsCompleted;
    private Integer repsCompleted;
    private Double weightUsed;
    private String notes;
    private LocalDateTime completedAt;
}