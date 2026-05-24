package com.iamfit.autenticacion_seguridad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.service.impl.AuthService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks de Mockito sin levantar Spring
        MockitoAnnotations.openMocks(this);

        // Configura MockMvc manualmente apuntando solo al controlador
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    private UserProfileRequest mockProfile() {
        return new UserProfileRequest("testuser", 25, 70, 175, "M");
    }
    @Test
    @DisplayName("Debería registrar un usuario y retornar 201 Created")
    void registerSuccessTest() throws Exception {
        RegisterRequest regReq = new RegisterRequest("test@mail.com", "Password123!");
        SessionRequest sessReq = new SessionRequest("dev-123", "Chrome-Linux");
        RegisterWrapper wrapper = new RegisterWrapper(regReq, mockProfile(), sessReq);
        AuthResponse response = new AuthResponse("access", "refresh", Instant.now());

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    @DisplayName("Debería hacer login y retornar 200 OK")
    void loginSuccessTest() throws Exception {
        LoginRequest logReq = new LoginRequest("test@mail.com", "Password123!", LoginProvider.LOCAL);
        SessionRequest sessReq = new SessionRequest("dev-123", "Chrome-Linux");
        LoginWrapper wrapper = new LoginWrapper(logReq, sessReq);
        AuthResponse response = new AuthResponse("access", "refresh", Instant.now());

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }
}
