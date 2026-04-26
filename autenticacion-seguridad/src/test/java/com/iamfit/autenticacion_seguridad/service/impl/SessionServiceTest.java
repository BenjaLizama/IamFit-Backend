package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.SessionEntity;
import com.iamfit.autenticacion_seguridad.exception.InvalidPasswordException;
import com.iamfit.autenticacion_seguridad.exception.RefreshTokenNotFoundException;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.repository.SessionRepository;
import com.iamfit.autenticacion_seguridad.util.ClientContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private CredentialRepository credentialRepository;
    @Mock private TokenHashService tokenHashService;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "refreshTokenDurationMs", 2592000000L);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debería crear una sesión exitosamente sanitizando el User Agent y usando DeviceID del Header")
    void createSession_Success() {
        UUID credentialId = UUID.randomUUID();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(credentialId);

        SessionRequest request = new SessionRequest("ID_IGNORADO", "iPhone 15");

        try (MockedStatic<ClientContextHolder> mockedContext = mockStatic(ClientContextHolder.class)) {
            // CONFIGURACIÓN CLAVE: Ahora necesitamos el deviceId del header
            mockedContext.when(ClientContextHolder::getDeviceId).thenReturn("device-from-header-123");
            mockedContext.when(ClientContextHolder::getIp).thenReturn("192.168.1.1");
            mockedContext.when(ClientContextHolder::getUserAgent).thenReturn("Mozilla/5.0  (Test)");

            when(credentialRepository.findById(credentialId)).thenReturn(Optional.of(credential));
            when(tokenHashService.hash(anyString())).thenReturn("hashed-token");
            when(sessionRepository.findByCredentialAndDeviceId(any(), anyString())).thenReturn(Optional.empty());

            SessionResponse response = sessionService.createSession(credentialId, request);

            assertNotNull(response);
            verify(sessionRepository).save(argThat(session ->
                    session.getDeviceId().equals("device-from-header-123") && // Verificamos que use el del header
                            session.getUserAgent().equals("Mozilla/5.0 (Test)") &&
                            session.getIsActive()
            ));
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción si el User Agent solo contiene caracteres de control")
    void createSession_Fail_InvalidUserAgent() {
        UUID credentialId = UUID.randomUUID();
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.of(new CredentialEntity()));

        try (MockedStatic<ClientContextHolder> mockedContext = mockStatic(ClientContextHolder.class)) {
            mockedContext.when(ClientContextHolder::getUserAgent).thenReturn("\n\r\t"); // Caracteres de control

            assertThrows(IllegalArgumentException.class, () ->
                    sessionService.createSession(credentialId, new SessionRequest("d", "n"))
            );
        }
    }

    @Test
    @DisplayName("Debería refrescar el token correctamente si la sesión es válida")
    void refreshToken_Success() {
        String rawToken = "old-refresh-token";
        String deviceId = "device-001";

        RefreshTokenWrapper wrapper = new RefreshTokenWrapper(
                new RefreshTokenRequest(rawToken),
                new SessionRequest(deviceId, "PC")
        );

        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID());

        SessionEntity sessionExistente = SessionEntity.builder()
                .credential(credential)
                .deviceId(deviceId)
                .expiryDate(Instant.now().plusSeconds(1000))
                .isActive(true)
                .build();

        when(tokenHashService.hash(rawToken)).thenReturn("hashed");
        when(sessionRepository.findByRefreshTokenHash("hashed")).thenReturn(Optional.of(sessionExistente));

        when(credentialRepository.findById(any())).thenReturn(Optional.of(credential));
        when(sessionRepository.findByCredentialAndDeviceId(any(), eq(deviceId))).thenReturn(Optional.of(sessionExistente));
        when(jwtService.generateToken(any())).thenReturn("new-access-token");

        // 3. Mocking Estático del Contexto
        try (MockedStatic<ClientContextHolder> mockedContext = mockStatic(ClientContextHolder.class)) {
            mockedContext.when(ClientContextHolder::getDeviceId).thenReturn(deviceId);
            mockedContext.when(ClientContextHolder::getIp).thenReturn("127.0.0.1");
            mockedContext.when(ClientContextHolder::getUserAgent).thenReturn("Mozilla/5.0");

            AuthResponse response = sessionService.refreshToken(wrapper);

            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());

            verify(sessionRepository).save(argThat(s -> s.getDeviceId().equals(deviceId)));
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción si la sesión expiró cronológicamente")
    void verifyExpiration_Fail_Expired() {
        SessionEntity session = new SessionEntity();
        session.setExpiryDate(Instant.now().minusSeconds(10));

        assertThrows(RefreshTokenNotFoundException.class, () -> sessionService.verifyExpiration(session));
        verify(sessionRepository).delete(session);
    }

    @Test
    @DisplayName("Debería lanzar RefreshTokenNotFoundException si el hash no existe")
    void refreshToken_Fail_NotFound() {
        when(tokenHashService.hash(anyString())).thenReturn("not-exists");
        when(sessionRepository.findByRefreshTokenHash(anyString())).thenReturn(Optional.empty());

        RefreshTokenWrapper wrapper = new RefreshTokenWrapper(
                new RefreshTokenRequest("token"),
                new SessionRequest("d", "n")
        );

        assertThrows(RefreshTokenNotFoundException.class, () -> sessionService.refreshToken(wrapper));
    }

    @Test
    @DisplayName("Debería cambiar la contraseña del usuario autenticado y revocar sus sesiones")
    void changePassword_Success() {
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID());
        credential.setEmail("user@test.com");
        credential.setPassword("encoded-old");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null)
        );

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");

        when(credentialRepository.findByEmail("user@test.com")).thenReturn(Optional.of(credential));
        when(credentialRepository.findById(credential.getId())).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("OldPass123!", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123!", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encoded-new");
        when(jwtService.generateToken(any())).thenReturn("new-access-token");
        when(tokenHashService.hash(anyString())).thenReturn("hashed-token");
        when(sessionRepository.findByCredentialAndDeviceId(any(), any())).thenReturn(Optional.empty());

        try (MockedStatic<ClientContextHolder> mockedContext = mockStatic(ClientContextHolder.class)) {
            mockedContext.when(ClientContextHolder::getDeviceId).thenReturn("device-001");
            mockedContext.when(ClientContextHolder::getIp).thenReturn("127.0.0.1");
            mockedContext.when(ClientContextHolder::getUserAgent).thenReturn("Mozilla/5.0");

            AuthResponse response = sessionService.changePassword(request);

            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());
            assertEquals("encoded-new", credential.getPassword());
            verify(credentialRepository).saveAndFlush(credential);
            verify(sessionRepository).revokeAllByCredentialId(credential.getId());
        }
    }

    @Test
    @DisplayName("Debería fallar si la contraseña actual no coincide")
    void changePassword_Fail_WrongCurrentPassword() {
        CredentialEntity credential = new CredentialEntity();
        credential.setEmail("user@test.com");
        credential.setPassword("encoded-old");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null)
        );

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass123!", "NewPass123!");

        when(credentialRepository.findByEmail("user@test.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("WrongPass123!", "encoded-old")).thenReturn(false);

        InvalidPasswordException ex = assertThrows(InvalidPasswordException.class,
                () -> sessionService.changePassword(request));

        assertEquals("La contraseña actual es incorrecta.", ex.getMessage());
        verify(credentialRepository, never()).save(any());
        verify(sessionRepository, never()).revokeAllByCredentialId(any());
    }

    @Test
    @DisplayName("Debería fallar si la nueva contraseña es igual a la actual")
    void changePassword_Fail_SamePassword() {
        CredentialEntity credential = new CredentialEntity();
        credential.setEmail("user@test.com");
        credential.setPassword("encoded-old");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null)
        );

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "OldPass123!");

        when(credentialRepository.findByEmail("user@test.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("OldPass123!", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("OldPass123!", "encoded-old")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sessionService.changePassword(request));

        assertEquals("La nueva contraseña no puede ser igual a la actual.", ex.getMessage());
        verify(credentialRepository, never()).save(any());
        verify(sessionRepository, never()).revokeAllByCredentialId(any());
    }

}
