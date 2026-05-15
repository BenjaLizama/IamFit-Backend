package com.iamfit.autenticacion_seguridad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${RSA_PRIVATE}")
    private String privateKeyStr;

    @Value("${RSA_PUBLIC}")
    private String publicKeyStr;

    @Bean
    public PrivateKey privateKey() throws Exception {
        String cleanKey = privateKeyStr.replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        String cleanKey = publicKeyStr.replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

}
