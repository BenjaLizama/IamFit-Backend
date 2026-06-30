package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExerciseDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        UUID id = UUID.randomUUID();
        ExerciseDto exercise = ExerciseDto.builder()
                .id(id)
                .name("Dominadas")
                .defaultSets(3)
                .defaultReps(10)
                .build();

        assertNotNull(exercise);
        assertEquals(id, exercise.getId());
        assertEquals("Dominadas", exercise.getName());
        assertEquals(3, exercise.getDefaultSets());
    }
}
