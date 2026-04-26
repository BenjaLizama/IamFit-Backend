package com.iamfit.autenticacion_seguridad.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandarErrorResponse (
        Integer status,                     // Ejemplo: 404.
        String code,                        // Código interno (ej: AUTH_001)
        String error,                       // Ejemplo: "NotFound".
        String message,                     // Aquí va el mensaje que se muestra en el frontend.
        String developerMessage,            // Aquí va el mensaje que pone el desarrollador.
        String path,                        // La ruta que falló (ej: /auth/login).
        Long timestamp,                     // La hora/minuto/segundo en que ocurrió el error.
        Map<String, String> validationError // Para errores de formularios.
) {}