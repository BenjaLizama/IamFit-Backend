package com.iamfit.autenticacion_seguridad.dto;

import java.time.Instant;

public record RefreshTokenResponse(
        String token,
        Instant expiryDate
) {
}
