package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditRoutineExerciseRequestTest {

    @Test
    @DisplayName("Debería asignar y obtener valores correctamente mediante Lombok @Data")
    void getterAndSetter() {
        EditRoutineExerciseRequest request = new EditRoutineExerciseRequest();
        request.setSets(4);
        request.setReps(8);
        request.setWeightKg(60.5);
        request.setRestSeconds(90);
        request.setNotes("Bajar lento");

        assertEquals(4, request.getSets());
        assertEquals(8, request.getReps());
        assertEquals(60.5, request.getWeightKg());
        assertEquals(90, request.getRestSeconds());
        assertEquals("Bajar lento", request.getNotes());
    }
}