package com.iamfit.autenticacion_seguridad.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Acá agrega un token a la blacklist con un TTL igual al tiempo que le queda de vida al token
     * @param token es el JWT a invalidar
     * @param ttlSeconds son los segundos de vida que le quedan
     */

    public void addToBlacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
        log.info("[BLACKLIST] Token agregado a la blacklist. TTL: {} segundos", ttlSeconds);
    }

    /**
     * Acá se verifica si un token está en la blacklist
     * @param token JWT a verificar
     * @return true si el token está revocado, false si es válido
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token)); // boolean por si llega a tirar null en algún moemnto
    }

}