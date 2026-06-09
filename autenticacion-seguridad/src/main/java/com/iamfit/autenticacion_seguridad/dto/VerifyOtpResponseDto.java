package com.iamfit.autenticacion_seguridad.dto;

public record VerifyOtpResponseDto(
        String status,
        String resetToken,
        String message
) {}