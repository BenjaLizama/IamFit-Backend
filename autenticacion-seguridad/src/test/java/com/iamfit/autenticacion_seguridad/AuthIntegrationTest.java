package com.iamfit.autenticacion_seguridad;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.repository.RoleRepository;
import com.iamfit.autenticacion_seguridad.repository.SessionRepository;
import com.iamfit.autenticacion_seguridad.service.impl.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(AuthIntegrationTest.TestAdminController.class) // Importamos directamente el controlador
class AuthIntegrationTest {

    // CONTROLADOR FAKE SIMPLIFICADO (Sin Config interna para evitar duplicidad)
    @RestController
    static class TestAdminController {
        @PostMapping("/api/v1/admin/test-protection")
        @PreAuthorize("hasRole('ADMIN')")
        public void fakeAdminRoute() {}
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CredentialRepository credentialRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SessionRepository sessionRepository;

    @MockitoBean private TokenBlacklistService tokenBlacklistService;

    private final String DEVICE_HEADER = "X-Device-ID";
    private final String DEFAULT_DEVICE = "test-device-id";

    @DynamicPropertySource
    static void overrideRsaProperties(DynamicPropertyRegistry registry) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            registry.add("RSA_PRIVATE", () -> Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));
            registry.add("RSA_PUBLIC", () -> Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
            registry.add("DEV_PORT", () -> "0");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @BeforeEach
    void setUp() {
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        sessionRepository.deleteAll();
        credentialRepository.deleteAll();

        if (!roleRepository.existsByRoleName("ROLE_USER")) {
            RoleEntity userRole = new RoleEntity();
            userRole.setRoleName("ROLE_USER");
            userRole.setIsActive(true);
            roleRepository.save(userRole);
        }
    }

    @Test
    @DisplayName("Flujo Completo: Registro -> Login exitoso")
    void fullAuthFlowTest() throws Exception {
        RegisterRequest regReq = new RegisterRequest("full@test.com", "Password123!");
        SessionRequest sessReq = new SessionRequest(DEFAULT_DEVICE, "Test-Station");

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(DEVICE_HEADER, DEFAULT_DEVICE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterWrapper(regReq, sessReq))))
                .andExpect(status().isCreated());

        LoginRequest logReq = new LoginRequest("full@test.com", "Password123!", LoginProvider.LOCAL);
        mockMvc.perform(post("/api/v1/auth/login")
                        .header(DEVICE_HEADER, DEFAULT_DEVICE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginWrapper(logReq, sessReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("Concurrencia: Re-uso de Refresh Token")
    void concurrentRefreshTokenTest() throws Exception {
        String deviceId = "refresh-device";
        SessionRequest sess = new SessionRequest(deviceId, "Node");
        RegisterWrapper reg = new RegisterWrapper(new RegisterRequest("ref@test.com", "Pass123!"), sess);

        String res = mockMvc.perform(post("/api/v1/auth/register")
                .header(DEVICE_HEADER, deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg))).andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readValue(res, AuthResponse.class);
        RefreshTokenWrapper wrap = new RefreshTokenWrapper(new RefreshTokenRequest(auth.refreshToken()), sess);

        mockMvc.perform(post("/api/v1/session/refresh-token")
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrap)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/session/refresh-token")
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrap)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Seguridad: ROLE_USER vs Admin Route (403)")
    void forbiddenAccessTest() throws Exception {
        String devId = "user-dev";
        String securePass = ".123Contrasena#";

        RegisterWrapper reg = new RegisterWrapper(
                new RegisterRequest("user@test.com", securePass),
                new SessionRequest(devId, "n")
        );

        // 1. Registro
        String res = mockMvc.perform(post("/api/v1/auth/register")
                        .header(DEVICE_HEADER, devId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readValue(res, AuthResponse.class);

        // 2. Acceso denegado (403)
        mockMvc.perform(post("/api/v1/admin/test-protection")
                        .header("Authorization", "Bearer " + auth.accessToken())
                        .header(DEVICE_HEADER, devId))
                .andExpect(status().isForbidden())
                // Actualizamos el código esperado según tus logs:
                .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN_001"));
    }

    @Test
    @DisplayName("Registro fallido: Email duplicado")
    void registerDuplicateEmailTest() throws Exception {
        RegisterRequest req = new RegisterRequest("dup@test.com", "Pass123!");
        mockMvc.perform(post("/api/v1/auth/register")
                .header(DEVICE_HEADER, "d1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterWrapper(req, new SessionRequest("d1", "n")))));

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(DEVICE_HEADER, "d2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterWrapper(req, new SessionRequest("d2", "n")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login fallido: Password incorrecto")
    void loginWrongPasswordTest() throws Exception {
        String dev = "login-dev";
        mockMvc.perform(post("/api/v1/auth/register")
                .header(DEVICE_HEADER, dev)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterWrapper(new RegisterRequest("p@test.com", "Correct123!"), new SessionRequest(dev, "n")))));

        LoginWrapper wrong = new LoginWrapper(new LoginRequest("p@test.com", "Wrong!", LoginProvider.LOCAL), new SessionRequest(dev, "n"));
        mockMvc.perform(post("/api/v1/auth/login")
                        .header(DEVICE_HEADER, dev)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrong)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sesión: Migración de sesión")
    void sessionMigrationTest() throws Exception {
        String email = "mig@test.com";
        String dev = "mig-dev";

        mockMvc.perform(post("/api/v1/auth/register")
                .header(DEVICE_HEADER, dev)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterWrapper(new RegisterRequest(email, ".123Contrasena#"), new SessionRequest(dev, "Old")))));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(DEVICE_HEADER, dev)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginWrapper(new LoginRequest(email, ".123Contrasena#", LoginProvider.LOCAL), new SessionRequest(dev, "New")))))
                .andExpect(status().isOk());

        assertThat(sessionRepository.findAll()).hasSize(1);
        assertThat(sessionRepository.findAll().get(0).getDeviceName()).isEqualTo("New");
    }

    @Test
    @DisplayName("Flujo Completo: Logout")
    void logoutFlowTest() throws Exception {
        String dev = "out-dev";
        String res = mockMvc.perform(post("/api/v1/auth/register")
                        .header(DEVICE_HEADER, dev)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterWrapper(new RegisterRequest("out@test.com", ".123Contrasena#"), new SessionRequest(dev, "n")))))
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readValue(res, AuthResponse.class);

        mockMvc.perform(post("/api/v1/session/logout")
                        .header("Authorization", "Bearer " + auth.accessToken())
                        .header(DEVICE_HEADER, dev))
                .andExpect(status().isNoContent());

        assertThat(sessionRepository.findAll().get(0).getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Flujo Completo: Cambio de contraseña")
    void changePasswordFlowTest() throws Exception {
        String email = "change@test.com";
        String oldPassword = ".123Contrasena#";
        String newPassword = "NuevaClave1!";
        String deviceId = "change-dev";

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterWrapper(
                                        new RegisterRequest(email, oldPassword),
                                        new SessionRequest(deviceId, "Phone")
                                ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readValue(registerResponse, AuthResponse.class);

        mockMvc.perform(post("/api/v1/session/change-password")
                        .header("Authorization", "Bearer " + auth.accessToken())
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(oldPassword, newPassword))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginWrapper(
                                        new LoginRequest(email, oldPassword, LoginProvider.LOCAL),
                                        new SessionRequest(deviceId, "Phone")
                                ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(DEVICE_HEADER, deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginWrapper(
                                        new LoginRequest(email, newPassword, LoginProvider.LOCAL),
                                        new SessionRequest(deviceId, "Phone")
                                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        assertThat(sessionRepository.findAll()).hasSize(1);
        assertThat(sessionRepository.findAll().get(0).getIsActive()).isTrue();
    }
}
