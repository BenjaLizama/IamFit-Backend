package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoutineExerciseDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        UUID id = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        RoutineExerciseDto exercise = RoutineExerciseDto.builder()
                .id(id)
                .exerciseId(exerciseId)
                .exerciseName("Peso Muerto")
                .sets(4)
                .reps(8)
                .weightKg(100.0)
                .restSeconds(120)
                .orderIndex(1)
                .notes("Mantener espalda recta")
                .build();

        assertNotNull(exercise);
        assertEquals(id, exercise.getId());
        assertEquals(exerciseId, exercise.getExerciseId());
        assertEquals("Peso Muerto", exercise.getExerciseName());
        assertEquals(4, exercise.getSets());
        assertEquals(100.0, exercise.getWeightKg());
        assertEquals(1, exercise.getOrderIndex());
    }
}