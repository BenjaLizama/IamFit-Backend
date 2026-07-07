package com.iamfit.ai_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest
class VertexAiServiceApplicationTests {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }

}