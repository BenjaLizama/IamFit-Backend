package com.iamfit.ejercicios.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SessionExerciseCompletionDto {
    private UUID sessionId;
    private UUID exerciseEntryId;
    private Boolean completed;
    private LocalDateTime completedAt;
}