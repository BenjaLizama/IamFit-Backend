package com.iamfit.autenticacion_seguridad.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {}
