package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

        jwtService = new JwtService(privateKey, publicKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900000L);
    }

    private SecurityCredential createMockUser(String email) {
        SecurityCredential user = mock(SecurityCredential.class);
        when(user.getUsername()).thenReturn(email);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getAuthorities()).thenReturn(Collections.emptyList());
        return user;
    }

    @Test
    @DisplayName("Debería validar un token correctamente")
    void isTokenValid_ShouldReturnTrue_WhenTokenIsCorrect() {
        SecurityCredential userDetails = createMockUser("user@test.com");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Debería retornar el tiempo restante de vida del token")
    void getRemainingTtlSeconds_ShouldReturnPositiveValue() {
        SecurityCredential userDetails = createMockUser("user@test.com");
        String token = jwtService.generateToken(userDetails);

        long ttl = jwtService.getRemainingTtlSeconds(token);

        assertTrue(ttl > 0 && ttl <= 900);
    }

    @Test
    @DisplayName("Debería generar un token con claims completos de Roles y Privilegios")
    void generateToken_ShouldContainCustomClaims() {
        UUID userId = UUID.randomUUID();
        SecurityCredential userDetails = mock(SecurityCredential.class);
        when(userDetails.getUsername()).thenReturn("admin@nomall.com");
        when(userDetails.getId()).thenReturn(userId);
        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("WRITE_PRIVILEGE")
        )).when(userDetails).getAuthorities();

        String token = jwtService.generateToken(userDetails);

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(userId.toString(), claims.get("userId"));

        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("ROLE_ADMIN"));

        List<String> privileges = (List<String>) claims.get("privileges");
        assertTrue(privileges.contains("WRITE_PRIVILEGE"));
    }

    @Test
    @DisplayName("Debería exportar la llave pública en Base64")
    void getPublicKeyAsBase64_ShouldReturnValidString() {
        String base64Key = jwtService.getPublicKeyAsBase64();
        assertNotNull(base64Key);
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(base64Key));
    }
}
