package com.iamfit.autenticacion_seguridad.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequestDto(
        @NotBlank(message = "El correo es obligatorio")
        String email,
        @NotBlank(message = "El código es obligatorio")
        String code
) {}