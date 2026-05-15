package com.iamfit.autenticacion_seguridad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.iamfit.autenticacion_seguridad.dto.AuthResponse;
import com.iamfit.autenticacion_seguridad.dto.ChangePasswordRequest;
import com.iamfit.autenticacion_seguridad.dto.RefreshTokenRequest;
import com.iamfit.autenticacion_seguridad.dto.RefreshTokenWrapper;
import com.iamfit.autenticacion_seguridad.dto.SessionRequest;
import com.iamfit.autenticacion_seguridad.service.impl.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(sessionController).build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    @Test
    @DisplayName("Debería refrescar el token y retornar 200 OK")
    void refreshTokenSuccessTest() throws Exception {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest("old-refresh-token");
        SessionRequest sessReq = new SessionRequest("dev-123", "Chrome-Linux");
        RefreshTokenWrapper wrapper = new RefreshTokenWrapper(refreshReq, sessReq);

        AuthResponse response = new AuthResponse("new-access", "new-refresh", Instant.now());

        when(sessionService.refreshToken(any(RefreshTokenWrapper.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/session/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

        verify(sessionService, times(1)).refreshToken(any());
    }

    @Test
    @DisplayName("Debería desactivar la sesión actual y retornar 204 No Content")
    void deactivateSelfSuccessTest() throws Exception {
        mockMvc.perform(patch("/api/v1/session/deactivate"))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).deactivateSelf();
    }

    @Test
    @DisplayName("Debería cambiar la contraseña y retornar 200 ok más tokens y refresh token y expiresAt")
    void changePasswordSuccessTest() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");

        mockMvc.perform(post("/api/v1/session/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Debería realizar logout y retornar 204 No Content")
    void logoutSuccessTest() throws Exception {
        mockMvc.perform(post("/api/v1/session/logout"))
                .andExpect(status().isNoContent());

        verify(sessionService, times(1)).logout();
    }
}
