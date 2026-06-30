package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutineDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        RoutineDto routine = RoutineDto.builder()
                .id(id)
                .name("Rutina Fuerza")
                .description("Rutina de fuerza para tren superior")
                .estimatedDurationMinutes(45)
                .aiGenerated(true)
                .createdAt(now)
                .exercises(new ArrayList<>())
                .build();

        assertNotNull(routine);
        assertEquals(id, routine.getId());
        assertEquals("Rutina Fuerza", routine.getName());
        assertEquals(45, routine.getEstimatedDurationMinutes());
        assertTrue(routine.getAiGenerated());
        assertEquals(now, routine.getCreatedAt());
        assertNotNull(routine.getExercises());
    }
}