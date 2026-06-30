package com.iamfit.ejercicios;

import com.iamfit.ejercicios.service.ExerciseCatalogService;
import com.iamfit.ejercicios.service.RoutineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.ai.vertex.ai.embedding.enabled=false"
        }
)
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RoutineService routineService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    ExerciseCatalogService exerciseCatalogService;

    @Test
    void shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/routines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/routines")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", "123"))))
                .andExpect(status().isOk());
    }
}