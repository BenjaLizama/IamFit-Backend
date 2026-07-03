package com.iamfit.ejercicios.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LogWorkoutRequest(
        LocalDate date,
        Integer durationMinutes,
        List<UUID> completedExerciseIds,
        String notes) {}