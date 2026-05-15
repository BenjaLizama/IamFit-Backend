package com.iamfit.autenticacion_seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String oldPassword,

        @NotBlank(message = "La contraseña nueva no puede estar vacía")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres.")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "La contraseña debe contener al menos un número, una mayúscula, una minúscula y un carácter especial."
        )
        String newPassword
) {
}