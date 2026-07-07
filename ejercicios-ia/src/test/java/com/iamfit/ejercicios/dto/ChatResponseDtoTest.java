package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatResponseDtoTest {

    @Test
    @DisplayName("Debería crear correctamente el record ChatResponseDto")
    void recordCreation() {
        ChatResponseDto response = new ChatResponseDto("¿Qué es hipertrofia?", "Es el crecimiento muscular.");

        assertEquals("¿Qué es hipertrofia?", response.pregunta());
        assertEquals("Es el crecimiento muscular.", response.respuesta());
    }
}