package com.iam_fit.ms_rutinas.dto;

public record ErrorResponseDto(
        int status,
        String mensaje
) {
}
