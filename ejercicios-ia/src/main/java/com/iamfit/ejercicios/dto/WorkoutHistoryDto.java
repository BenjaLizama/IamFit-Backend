package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.WorkoutHistory.WorkoutStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkoutHistoryDto {
    private UUID id;
    private LocalDate workoutDate;
    private String routineName;
    private WorkoutStatus status;
    private LocalDateTime completedAt;
    private String notes;
}