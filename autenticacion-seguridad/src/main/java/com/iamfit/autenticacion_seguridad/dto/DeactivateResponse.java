package com.iamfit.autenticacion_seguridad.dto;

import java.time.Instant;

public record DeactivateResponse(
        String message,
        Instant deactivatedAt
) {}
