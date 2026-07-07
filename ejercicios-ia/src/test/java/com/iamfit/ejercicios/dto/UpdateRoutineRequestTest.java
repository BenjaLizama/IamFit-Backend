package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateRoutineRequestTest {

    @Test
    @DisplayName("Debería asignar y obtener valores correctamente mediante Lombok @Data")
    void getterAndSetter() {
        UpdateRoutineRequest request = new UpdateRoutineRequest();
        request.setName("Rutina Actualizada");
        request.setDescription("Nueva descripción");
        request.setEstimatedDurationMinutes(60);

        assertEquals("Rutina Actualizada", request.getName());
        assertEquals("Nueva descripción", request.getDescription());
        assertEquals(60, request.getEstimatedDurationMinutes());
        assertNull(request.getDifficultyLevel()); // Verificamos que maneja nulos correctamente
    }
}