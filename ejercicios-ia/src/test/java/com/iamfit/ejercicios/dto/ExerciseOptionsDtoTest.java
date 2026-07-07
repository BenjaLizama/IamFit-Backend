package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExerciseOptionsDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        ExerciseOptionsDto options = ExerciseOptionsDto.builder()
                .muscleGroups(List.of())
                .equipment(List.of())
                .difficultyLevels(List.of())
                .build();

        assertNotNull(options.getMuscleGroups());
        assertTrue(options.getEquipment().isEmpty());
    }
}