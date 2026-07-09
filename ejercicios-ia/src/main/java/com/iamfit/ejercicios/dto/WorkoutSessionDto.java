package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.WorkoutSession.SessionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WorkoutSessionDto {
    private UUID sessionId;
    private UUID routineId;
    private String routineName;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalExercises;
    private Integer completedExercises;
    private Integer progressPercentage;
    private List<SessionExerciseDto> exercises;
}