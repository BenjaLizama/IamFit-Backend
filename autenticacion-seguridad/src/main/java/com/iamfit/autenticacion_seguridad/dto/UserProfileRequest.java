package com.iamfit.autenticacion_seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileRequest(
        String nickname,
        Integer age,
        Integer weight,
        Integer height,
        @JsonProperty("sexo") String sex
) {
}
