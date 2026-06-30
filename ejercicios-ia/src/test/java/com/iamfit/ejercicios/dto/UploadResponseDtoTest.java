package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UploadResponseDtoTest {

    @Test
    @DisplayName("Debería crear correctamente el record UploadResponseDto")
    void recordCreation() {
        UploadResponseDto response = new UploadResponseDto("Carga exitosa", "video_ejercicio.mp4");

        assertEquals("Carga exitosa", response.respuesta());
        assertEquals("video_ejercicio.mp4", response.nombre());
    }
}