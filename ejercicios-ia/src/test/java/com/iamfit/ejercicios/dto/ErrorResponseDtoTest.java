package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseDtoTest {

    @Test
    @DisplayName("Debería crear correctamente el record ErrorResponseDto")
    void recordCreation() {
        ErrorResponseDto error = new ErrorResponseDto(404, "Recurso no encontrado");

        assertEquals(404, error.status());
        assertEquals("Recurso no encontrado", error.mensaje());
    }
}