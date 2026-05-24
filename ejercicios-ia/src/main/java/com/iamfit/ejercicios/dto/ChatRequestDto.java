package com.iamfit.ejercicios.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(
        @NotBlank(message = "la pregunta no puede esta vacia")
        String pregunta
) {
}
