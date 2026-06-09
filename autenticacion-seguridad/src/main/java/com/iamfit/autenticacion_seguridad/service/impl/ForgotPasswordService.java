package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iamfit.autenticacion_seguridad.repository.SessionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final CredentialRepository credentialRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    @Value("${iamfit.otp.expiration-minutes:10}")
    private int expirationMinutes;

    // ─── Solicitar código OTP ────────────────────────────────────────

    public PasswordResetResponseDto requestOtp(ForgotPasswordRequestDto request) {
        // Por seguridad siempre retorna éxito aunque el email no exista
        boolean emailExists = credentialRepository.existsByEmail(request.email());

        if (emailExists) {
            String otp = otpService.generateAndSaveOtp(request.email());
            emailService.sendOtpEmail(request.email(), otp, expirationMinutes);
        } else {
            log.info("[FORGOT-PASSWORD] Email no registrado: {} — respuesta neutral", request.email());
        }

        return new PasswordResetResponseDto("SUCCESS", "Si el correo está registrado, recibirás un código en breve.");
    }

    // ─── Verificar OTP ───────────────────────────────────────────────

    public VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto request) {
        String resetToken = otpService.verifyOtp(request.email(), request.code());
        return new VerifyOtpResponseDto("SUCCESS", resetToken, "Código válido.");
    }

    // ─── Restablecer contraseña ──────────────────────────────────────

    @Transactional
    public PasswordResetResponseDto resetPassword(ResetPasswordRequestDto request) {
        String email = otpService.validateResetToken(request.resetToken());

        credentialRepository.findByEmail(email).ifPresent(credential -> {
            credential.setPassword(passwordEncoder.encode(request.newPassword()));
            credentialRepository.saveAndFlush(credential);
            sessionRepository.revokeAllByCredentialId(credential.getId());
            log.info("[FORGOT-PASSWORD] Contraseña restablecida para: {}", email);
        });

        return new PasswordResetResponseDto("SUCCESS", "Contraseña actualizada correctamente.");
    }
}