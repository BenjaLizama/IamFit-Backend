package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkoutHistoryDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        LocalDateTime completedAt = LocalDateTime.now();

        WorkoutHistoryDto history = WorkoutHistoryDto.builder()
                .id(id)
                .workoutDate(date)
                .routineName("Día de Pierna")
                .completedAt(completedAt)
                .notes("Me sentí con mucha energía")
                .build();

        assertNotNull(history);
        assertEquals(id, history.getId());
        assertEquals(date, history.getWorkoutDate());
        assertEquals("Día de Pierna", history.getRoutineName());
        assertEquals(completedAt, history.getCompletedAt());
        assertEquals("Me sentí con mucha energía", history.getNotes());
    }
}