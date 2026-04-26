package com.iamfit.autenticacion_seguridad.dto;

import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

        @NotBlank(message = "El identificador (email o token) es obligatorio.")
        String identifier,

        String password, // Opcional: Obligatorio para local, nulo para GOOGLE

        @NotNull(message = "El proveedor es obligatorio.")
        LoginProvider provider
) {}
