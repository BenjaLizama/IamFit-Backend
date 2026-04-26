package com.iamfit.autenticacion_seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LoginWrapper(
        @JsonProperty("login")
        @NotNull
        @Valid
        LoginRequest login,

        @JsonProperty("session")
        @NotNull
        @Valid
        SessionRequest session
) {
}
