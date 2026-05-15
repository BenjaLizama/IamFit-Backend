package com.iamfit.autenticacion_seguridad.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenHashServiceTest {

    @InjectMocks
    private TokenHashService tokenHashService;

    @Test
    @DisplayName("Debería generar un hash SHA-256 consistente para un token dado")
    void hash_Success() {
        String token = "my-secret-token-123";

        // Hash real de "my-secret-token-123"
        String expectedHash = "3e5358294ba0321e7b4a6acd192104f2e9c9776e8645bc24d4332e266596cc72";

        String result = tokenHashService.hash(token);

        assertNotNull(result);
        assertEquals(64, result.length(), "SHA-256 debe tener 64 caracteres hexadecimales");
        assertEquals(expectedHash, result, "El hash generado no coincide con el esperado");
    }

    @Test
    @DisplayName("Debería generar el mismo hash para entradas idénticas")
    void hash_Consistency() {
        String token = "consistency-test";

        String firstHash = tokenHashService.hash(token);
        String secondHash = tokenHashService.hash(token);

        assertEquals(firstHash, secondHash);
    }

    @Test
    @DisplayName("Debería generar hashes diferentes para entradas diferentes")
    void hash_DifferentInputs() {
        String tokenA = "token-a";
        String tokenB = "token-b";

        String hashA = tokenHashService.hash(tokenA);
        String hashB = tokenHashService.hash(tokenB);

        assertNotEquals(hashA, hashB);
    }
}