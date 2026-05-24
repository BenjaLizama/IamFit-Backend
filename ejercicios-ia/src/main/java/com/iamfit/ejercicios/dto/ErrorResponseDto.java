package com.iamfit.ejercicios.dto;

public record ErrorResponseDto(
        int status,
        String mensaje
) {
}
