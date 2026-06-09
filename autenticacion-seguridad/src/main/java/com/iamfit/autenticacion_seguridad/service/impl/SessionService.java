package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.SessionEntity;
import com.iamfit.autenticacion_seguridad.exception.CredentialNotFoundException;
import com.iamfit.autenticacion_seguridad.exception.InvalidPasswordException;
import com.iamfit.autenticacion_seguridad.exception.RefreshTokenNotFoundException;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.repository.SessionRepository;
import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import com.iamfit.autenticacion_seguridad.service.ISessionService;
import com.iamfit.autenticacion_seguridad.util.ClientContextHolder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService implements ISessionService {

    @Value("${jwt.expiration.refresh-token:2592000000}") // 30 días
    private Long refreshTokenDurationMs;

    private final SessionRepository sessionRepository;
    private final CredentialRepository credentialRepository;
    private final TokenHashService tokenHashService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;



    public void verifyExpiration(SessionEntity session) {
        if (session.getExpiryDate().isBefore(Instant.now())) {
            sessionRepository.delete(session);
            throw new RefreshTokenNotFoundException("El Refresh Token ha expirado cronológicamente en la base de datos.");
        }
        if (!session.getIsActive()) {
            sessionRepository.delete(session);
            throw new RefreshTokenNotFoundException("La sesión ha sido marcada como inactiva (revocada).");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenWrapper wrapper) {
        String rawToken = wrapper.token().requestToken().trim();
        String hashed = tokenHashService.hash(rawToken);

        SessionEntity session = sessionRepository.findByRefreshTokenHash(hashed)
                .orElseThrow(() -> new RefreshTokenNotFoundException("No se encontró una sesión activa para el hash del Refresh Token proporcionado."));

        verifyExpiration(session);

        if (!session.getDeviceId().equals(wrapper.session().deviceId())) {
            throw new SecurityException("El dispositivo no coincide con la sesión iniciada.");
        }

        SecurityCredential userDetails = new SecurityCredential(session.getCredential());
        String newAccessToken = jwtService.generateToken(userDetails);

        // Rotación de sesión
        SessionResponse newSession = createSession(
                session.getCredential().getId(),
                wrapper.session()
        );

        return new AuthResponse(newAccessToken, newSession.rawRefreshToken(), newSession.expiryDate());
    }

    @Transactional
    public void revokeSession(String rawRefreshToken) {
        String hashed = tokenHashService.hash(rawRefreshToken);
        sessionRepository.findByRefreshTokenHash(hashed)
                .ifPresent(sessionRepository::delete);
    }

    @Override
    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        CredentialEntity credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new CredentialNotFoundException("Usuario no encontrado."));

        // 422 contraseña incorrecta
        if (!passwordEncoder.matches(request.currentPassword(), credential.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta.");
        }

        if (passwordEncoder.matches(request.newPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual.");
        }

        credential.setPassword(passwordEncoder.encode(request.newPassword()));
        credentialRepository.saveAndFlush(credential);
        sessionRepository.revokeAllByCredentialId(credential.getId());

        // Generar nuevos tokens con la sesión actualizada
        SecurityCredential userDetails = new SecurityCredential(credential);
        String accessToken = jwtService.generateToken(userDetails);
        SessionResponse session = createSession(credential.getId(), new SessionRequest(
                ClientContextHolder.getDeviceId(),
                ClientContextHolder.getDeviceId()
        ));

        log.info("[SECURITY] Contraseña actualizada para el usuario: {}. Sesiones revocadas.", email);

        return new AuthResponse(accessToken, session.rawRefreshToken(), session.expiryDate());
    }

    @Override
    @Transactional
    public DeactivateResponse deactivateSelf() {
        // 1. Obtenemos el correo del usuario en base al contexto del token.
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        CredentialEntity credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new CredentialNotFoundException("Usuario no encontrado."));

        // 2. Revocamos todas las sesiones (Utilizando el update masivo)
        sessionRepository.revokeAllByCredentialId(credential.getId());

        // 3. Desactivamos la cuenta.
        credentialRepository.deactivateById(credential.getId());

        log.info("[SECURITY] Cuenta desactivada: {}. Trazabilidad temporal sincronizada.", email);
        return new DeactivateResponse("Cuenta desactivada con éxito.", Instant.now());
    }



    @Override
    @Transactional
    public void logout() {
        String accessToken = ClientContextHolder.getToken();
        String deviceId = ClientContextHolder.getDeviceId();

        if (accessToken == null || deviceId == null) {
            throw new SecurityException("Información de sesión incompleta para logout.");
        }

        // 1. Blacklist
        long ttlSeconds = jwtService.getRemainingTtlSeconds(accessToken);
        tokenBlacklistService.addToBlacklist(accessToken, ttlSeconds);

        // 2. Revocación Selectiva
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CredentialEntity credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new CredentialNotFoundException("Usuario no encontrado."));

        sessionRepository.revokeByCredentialIdAndDeviceId(credential.getId(), deviceId);

        log.info("[LOGOUT] Sesión cerrada para el dispositivo: {}", deviceId);
    }


    @Override
    @Transactional
    public SessionResponse createSession(UUID credentialId, SessionRequest sessionRequest) {
        CredentialEntity credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new CredentialNotFoundException("Credencial no encontrada."));

        String deviceId = ClientContextHolder.getDeviceId();

        if (deviceId == null || deviceId.isBlank()) {
            log.error("[SECURITY] Intento de creación de sesión sin X-Device-ID Header.");
            throw new IllegalArgumentException("El header X-Device-ID es obligatorio.");
        }

        // Eliminar sesión previa de forma segura usando query directa
        // evita OptimisticLockException en requests concurrentes
        safeDeleteExistingSession(credential, deviceId);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = tokenHashService.hash(rawToken);

        SessionEntity session = SessionEntity.builder()
                .credential(credential)
                .refreshTokenHash(hashedToken)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .deviceId(deviceId)
                .deviceName(sessionRequest.deviceName())
                .ipAddress(ClientContextHolder.getIp())
                .userAgent(sanitizeUserAgent(ClientContextHolder.getUserAgent()))
                .isActive(true)
                .build();

        sessionRepository.save(session);
        return new SessionResponse(rawToken, session.getExpiryDate(), session.getDeviceName());
    }

    private void safeDeleteExistingSession(CredentialEntity credential, String deviceId) {
        try {
            sessionRepository.findByCredentialAndDeviceId(credential, deviceId)
                    .ifPresent(existing -> {
                        sessionRepository.deleteById(existing.getId());
                        sessionRepository.flush();
                    });
        } catch (Exception e) {
            log.warn("[SESSION] Sesión previa ya eliminada por request concurrente — deviceId: {}. Continuando login.", deviceId);
            // ignorar — otro request ya limpió la sesión
        }
    }

    private String sanitizeUserAgent(String ua) {
        if (ua == null) {
            return null;
        }
        // hace un control de caracteres y los saca
        String cleaned = ua.replaceAll("\\p{Cntrl}", "");
        // si encuentra 2 espacios los hace 1 y hace trim
        cleaned = cleaned.strip().replaceAll("\\s+", " ");
        // si después de normalizar queda vacío, viene vacío
        if (cleaned.isBlank()) {
            log.warn("[SECURITY] userAgent quedó vacío tras sanitización.");
            throw new IllegalArgumentException("El user agent no puede estar vacío.");
        }
        return cleaned;
    }

}
