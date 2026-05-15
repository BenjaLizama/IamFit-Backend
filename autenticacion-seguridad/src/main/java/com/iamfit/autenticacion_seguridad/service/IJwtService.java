package com.iamfit.autenticacion_seguridad.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String getPublicKeyAsBase64();
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    Claims extractAllClaims(String token);
    long getRemainingTtlSeconds(String token);
}
