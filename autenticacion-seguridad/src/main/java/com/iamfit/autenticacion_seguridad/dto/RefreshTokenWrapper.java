package com.iamfit.autenticacion_seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenWrapper (
        @JsonProperty("token")
        @NotNull(message = "Los datos del token son obligatorios.")
        @Valid
        RefreshTokenRequest token,

        @JsonProperty("session")
        @NotNull(message = "Los datos de sesión son obligatorios.")
        @Valid
        SessionRequest session
) {}
