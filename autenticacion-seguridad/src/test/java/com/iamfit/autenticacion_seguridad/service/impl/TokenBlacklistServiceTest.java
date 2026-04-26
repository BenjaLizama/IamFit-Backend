package com.iamfit.autenticacion_seguridad.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("Debería agregar un token a la blacklist con el TTL correcto")
    void addToBlacklist_Success() {
        String token = "mock-jwt-token";
        long ttl = 3600L;
        String expectedKey = "blacklist:" + token;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.addToBlacklist(token, ttl);

        verify(valueOperations).set(expectedKey, "revoked", ttl, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Debería retornar true si el token existe en Redis")
    void isBlacklisted_True() {
        String token = "revoked-token";
        String expectedKey = "blacklist:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertTrue(result);
        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    @DisplayName("Debería retornar false si el token no existe en Redis")
    void isBlacklisted_False() {
        String token = "valid-token";
        String expectedKey = "blacklist:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertFalse(result);
    }

    @Test
    @DisplayName("Debería manejar null de Redis y retornar false")
    void isBlacklisted_HandleNull() {
        String token = "unknown-token";

        when(redisTemplate.hasKey(anyString())).thenReturn(null);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertFalse(result);
    }
}