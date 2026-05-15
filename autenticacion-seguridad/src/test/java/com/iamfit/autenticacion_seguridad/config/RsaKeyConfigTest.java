package com.iamfit.autenticacion_seguridad.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RsaKeyConfigTest {

    private RsaKeyConfig rsaKeyConfig;
    private String validPublicBase64;
    private String validPrivateBase64;

    @BeforeEach
    void setUp() throws Exception {
        rsaKeyConfig = new RsaKeyConfig();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        validPublicBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + "\n  \r";
        validPrivateBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + "  \t";

        ReflectionTestUtils.setField(rsaKeyConfig, "publicKeyStr", validPublicBase64);
        ReflectionTestUtils.setField(rsaKeyConfig, "privateKeyStr", validPrivateBase64);
    }

    @Test
    @DisplayName("Debería generar un Bean de PrivateKey válido a partir de un String Base64")
    void privateKeyBeanTest() throws Exception {
        PrivateKey privateKey = rsaKeyConfig.privateKey();

        assertNotNull(privateKey);
        assertEquals("RSA", privateKey.getAlgorithm());
        assertEquals("PKCS#8", privateKey.getFormat());
    }

    @Test
    @DisplayName("Debería generar un Bean de PublicKey válido a partir de un String Base64")
    void publicKeyBeanTest() throws Exception {
        PublicKey publicKey = rsaKeyConfig.publicKey();

        assertNotNull(publicKey);
        assertEquals("RSA", publicKey.getAlgorithm());
        assertEquals("X.509", publicKey.getFormat());
    }

    @Test
    @DisplayName("Debería lanzar excepción si el String de la llave privada es inválido")
    void privateKeyInvalidTest() {
        ReflectionTestUtils.setField(rsaKeyConfig, "privateKeyStr", "clave-invalida-no-base64");

        assertThrows(Exception.class, () -> rsaKeyConfig.privateKey());
    }

    @Test
    @DisplayName("Debería lanzar excepción si el String de la llave pública es inválido")
    void publicKeyInvalidTest() {
        ReflectionTestUtils.setField(rsaKeyConfig, "publicKeyStr", "clave-invalida-no-base64");

        assertThrows(Exception.class, () -> rsaKeyConfig.publicKey());
    }
}
