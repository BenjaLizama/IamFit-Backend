package com.iamfit.autenticacion_seguridad.dto;

// TODO (lista): UTILIZAR VALIDATIONS AQUI

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El token de refresco es obligatorio")
        String requestToken
) {
}
