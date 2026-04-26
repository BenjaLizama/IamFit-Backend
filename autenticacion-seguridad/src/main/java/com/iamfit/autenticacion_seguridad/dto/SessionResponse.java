package com.iamfit.autenticacion_seguridad.dto;

import java.time.Instant;

public record SessionResponse(
        String rawRefreshToken,
        Instant expiryDate,
        String deviceName
) {
}
