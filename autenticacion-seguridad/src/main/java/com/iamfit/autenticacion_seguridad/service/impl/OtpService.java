package com.iamfit.autenticacion_seguridad.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    @Value("${iamfit.otp.expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${iamfit.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${iamfit.otp.length:6}")
    private int otpLength;

    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPTS_PREFIX = "otp:attempts:";
    private static final String RESET_TOKEN_PREFIX = "otp:reset:";

    // ─── Generar y guardar OTP ───────────────────────────────────────

    public String generateAndSaveOtp(String email) {
        String otp = generateNumericOtp();
        String key = OTP_PREFIX + email.toLowerCase();

        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(expirationMinutes));

        // Resetear intentos
        redisTemplate.delete(ATTEMPTS_PREFIX + email.toLowerCase());

        log.info("[OTP] Código generado para: {}", email);
        return otp;
    }

    // ─── Verificar OTP ───────────────────────────────────────────────

    public String verifyOtp(String email, String code) {
        String normalizedEmail = email.toLowerCase();
        String key = OTP_PREFIX + normalizedEmail;
        String attemptsKey = ATTEMPTS_PREFIX + normalizedEmail;

        // Verificar intentos máximos
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= maxAttempts) {
            log.warn("[OTP] Máximo de intentos alcanzado para: {}", email);
            throw new IllegalStateException("Máximo de intentos alcanzado. Solicita un nuevo código.");
        }

        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("[OTP] Código expirado o inexistente para: {}", email);
            throw new IllegalArgumentException("El código ha expirado o no existe. Solicita uno nuevo.");
        }

        if (!storedOtp.equals(code.trim())) {
            // Incrementar intentos
            redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(expirationMinutes));
            log.warn("[OTP] Código incorrecto para: {} — intento {}", email, attempts + 1);
            throw new IllegalArgumentException("Código incorrecto.");
        }

        // OTP válido — eliminar y generar reset token
        redisTemplate.delete(key);
        redisTemplate.delete(attemptsKey);

        String resetToken = UUID.randomUUID().toString();
        String resetKey = RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.opsForValue().set(resetKey, normalizedEmail, Duration.ofMinutes(15));

        log.info("[OTP] Código verificado correctamente para: {}", email);
        return resetToken;
    }

    // ─── Verificar reset token ───────────────────────────────────────

    public String validateResetToken(String resetToken) {
        String key = RESET_TOKEN_PREFIX + resetToken;
        String email = redisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new IllegalArgumentException("El token de restablecimiento ha expirado o es inválido.");
        }

        redisTemplate.delete(key);
        return email;
    }

    // ─── Helper ──────────────────────────────────────────────────────

    private String generateNumericOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}