package com.iam_fit.ms_rutinas.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(
        @NotBlank(message = "la pregunta no puede esta vacia")
        String pregunta
) {
}
