package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.exception.EmailAlreadyExistsException;
import com.iamfit.autenticacion_seguridad.exception.RoleNotFoundException;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private CredentialRepository credentialRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthStrategyManager authStrategyManager;
    @Mock private SessionService sessionService;

    @InjectMocks
    private AuthService authService;

    private UserProfileRequest mockProfile() {
        return new UserProfileRequest("testuser", 25, 70, 175, "M");
    }
    // Métodos de ayuda corregidos con Instant
    private SessionRequest createMockSessionRequest() {
        return new SessionRequest("device-001", "Chrome/Windows");
    }

    private SessionResponse createMockSessionResponse() {
        // Corregido: Usamos Instant y los 3 parámetros que requiere tu record
        return new SessionResponse(
                "mock-refresh-token",
                Instant.now().plus(1, ChronoUnit.DAYS),
                "session-metadata-example"
        );
    }

    @Test
    @DisplayName("Debería registrar un usuario exitosamente cuando los datos son válidos")
    void register_Success() {
        RegisterRequest regReq = new RegisterRequest("test@nomall.com", "password123");
        SessionRequest sessReq = createMockSessionRequest();
        RegisterWrapper wrapper = new RegisterWrapper(regReq, mockProfile(), sessReq);


        RoleEntity userRole = new RoleEntity();
        userRole.setRoleName("ROLE_USER");

        when(credentialRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        CredentialEntity savedCredential = new CredentialEntity();
        savedCredential.setId(UUID.randomUUID());
        savedCredential.setEmail(regReq.email());
        when(credentialRepository.save(any(CredentialEntity.class))).thenReturn(savedCredential);

        when(jwtService.generateToken(any())).thenReturn("mock-access-token");
        when(sessionService.createSession(any(UUID.class), any())).thenReturn(createMockSessionResponse());

        AuthResponse response = authService.register(wrapper);

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        // El assert de refreshToken ahora debería apuntar al campo del record SessionResponse
        assertNotNull(response.refreshToken());
        verify(credentialRepository).save(any());
    }

    @Test
    @DisplayName("Debería realizar el login correctamente")
    void login_Success() {
        LoginRequest loginReq = new LoginRequest("test@mail.com", "pass", LoginProvider.LOCAL);
        LoginWrapper wrapper = new LoginWrapper(loginReq, createMockSessionRequest());

        CredentialEntity mockCredential = new CredentialEntity();
        mockCredential.setId(UUID.randomUUID());

        when(authStrategyManager.executeStrategy(any())).thenReturn(mockCredential);
        when(jwtService.generateToken(any())).thenReturn("token-jwt");
        when(sessionService.createSession(any(), any())).thenReturn(createMockSessionResponse());

        AuthResponse response = authService.login(wrapper);

        assertNotNull(response);
        assertEquals("token-jwt", response.accessToken());
        verify(authStrategyManager).executeStrategy(loginReq);
    }

    @Test
    @DisplayName("Debería lanzar EmailAlreadyExistsException")
    void register_Fail_EmailExists() {
        RegisterWrapper wrapper = new RegisterWrapper(
                new RegisterRequest("existing@mail.com", "pass"),
                mockProfile(),
                createMockSessionRequest()
        );
        when(credentialRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(wrapper));
    }

    @Test
    @DisplayName("Debería lanzar RoleNotFoundException")
    void register_Fail_RoleNotFound() {
        RegisterWrapper wrapper = new RegisterWrapper(
                new RegisterRequest("test@mail.com", "pass"),
                mockProfile(),
                createMockSessionRequest()
        );
        when(credentialRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> authService.register(wrapper));
    }
}
