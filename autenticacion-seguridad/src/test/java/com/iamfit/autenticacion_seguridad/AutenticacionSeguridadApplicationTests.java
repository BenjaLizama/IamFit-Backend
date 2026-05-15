package com.iamfit.autenticacion_seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
class AutenticacionSeguridadApplicationTests {

    @org.springframework.test.context.DynamicPropertySource
    static void overrideRsaProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            java.security.KeyPair kp = kpg.generateKeyPair();
            String priv = java.util.Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
            String pub = java.util.Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            registry.add("RSA_PRIVATE", () -> priv);
            registry.add("RSA_PUBLIC", () -> pub);
            registry.add("DEV_PORT", () -> "0");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void contextLoads() {

    }

}