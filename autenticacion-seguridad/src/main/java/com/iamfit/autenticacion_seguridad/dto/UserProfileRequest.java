package com.iamfit.autenticacion_seguridad.dto;

public record UserProfileRequest(
        String nickname,
        Integer age,
        Integer weight,
        Integer height,
        String sex
) {
}
