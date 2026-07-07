package com.iamfit.ai_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ai_service.configuration.SecurityConfig;
import com.iamfit.ai_service.dto.*;
import com.iamfit.ai_service.service.WellbeingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WellbeingController.class)
@Import(SecurityConfig.class)
class WellbeingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WellbeingService wellbeingService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String TEST_USER_ID = "USER_123456";
    private final String VALID_TOKEN = "Bearer token-falso";
    private Jwt dummyJwt;

    @BeforeEach
    void setUp() {
        dummyJwt = Jwt.withTokenValue("token-falso")
                .header("alg", "none")
                .claim("userId", TEST_USER_ID)
                .build();

        when(jwtDecoder.decode(anyString())).thenReturn(dummyJwt);
    }

    @Test
    @DisplayName("checkIn() - Debe retornar 200 OK cuando el request es válido")
    void checkIn_Success() throws Exception {
        // Arrange
        WellbeingCheckInRequestDTO requestDTO = new WellbeingCheckInRequestDTO();
        // 👇 Corregimos el 400 asignando valores válidos que pasen las restricciones @Min/@Max
        requestDTO.setEstadoAnimo(5);
        requestDTO.setNivelEstres(5);
        requestDTO.setNota("Todo bien el día de hoy");

        // Construimos la respuesta usando la propiedad que el JSON de verdad expone
        WellbeingResponseDTO mockResponse = WellbeingResponseDTO.builder()
                .response("Registro de bienestar guardado exitosamente.")
                .build();

        when(wellbeingService.checkIn(eq(TEST_USER_ID), any(WellbeingCheckInRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wellbeing/check-in")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                // 👇 Corregido: Buscamos $.response en lugar de $.content
                .andExpect(jsonPath("$.response").value("Registro de bienestar guardado exitosamente."));

        verify(wellbeingService, times(1)).checkIn(eq(TEST_USER_ID), any(WellbeingCheckInRequestDTO.class));
    }

    @Test
    @DisplayName("getMotivation() - Debe retornar 200 OK y frase motivacional")
    void getMotivation_Success() throws Exception {
        // Arrange
        MotivationRequestDTO requestDTO = new MotivationRequestDTO();

        WellbeingResponseDTO mockResponse = WellbeingResponseDTO.builder()
                .response("El único modo de hacer un gran trabajo es amar lo que haces.")
                .build();

        when(wellbeingService.getMotivation(eq(TEST_USER_ID), any(MotivationRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wellbeing/motivation")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                // 👇 Corregido: Buscamos $.response
                .andExpect(jsonPath("$.response").value("El único modo de hacer un gran trabajo es amar lo que haces."));

        verify(wellbeingService, times(1)).getMotivation(eq(TEST_USER_ID), any(MotivationRequestDTO.class));
    }

    @Test
    @DisplayName("getTechnique() - Debe retornar 200 OK y la técnica solicitada")
    void getTechnique_Success() throws Exception {
        // Arrange
        TechniqueRequestDTO requestDTO = new TechniqueRequestDTO();

        WellbeingResponseDTO mockResponse = WellbeingResponseDTO.builder()
                .response("Prueba la técnica de respiración 4-7-8 para calmar la ansiedad.")
                .build();

        when(wellbeingService.getTechnique(eq(TEST_USER_ID), any(TechniqueRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wellbeing/techniques")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                // 👇 Corregido: Buscamos $.response
                .andExpect(jsonPath("$.response").value("Prueba la técnica de respiración 4-7-8 para calmar la ansiedad."));

        verify(wellbeingService, times(1)).getTechnique(eq(TEST_USER_ID), any(TechniqueRequestDTO.class));
    }
}