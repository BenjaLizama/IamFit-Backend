package com.iamfit.autenticacion_seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterWrapper(
        @JsonProperty("register")
        @NotNull(message = "Los datos del registro son obligatorios.")
        @Valid
        RegisterRequest register,

        @JsonProperty("UserProfile")
        @NotNull(message = "Los datos del perfil de usuario son obligatorios.")
        @Valid
        UserProfileRequest userProfile,

        @JsonProperty("session")
        @NotNull(message = "Los datos de sesión son obligatorios.")
        @Valid
        SessionRequest session
) {}
