package com.iamfit.ejercicios.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RoutineProgressDto {
    private java.util.UUID routineId;
    private Long completedWorkouts;
    private Integer currentStreak;
    private LocalDateTime lastCompletedAt;
    private Integer weeklyCompletionPercentage;
}