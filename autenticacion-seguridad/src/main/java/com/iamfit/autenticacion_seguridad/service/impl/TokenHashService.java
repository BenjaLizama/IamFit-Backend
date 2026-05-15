package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.service.ITokenHashService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class TokenHashService implements ITokenHashService {

    @Override
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte _byte : hash) {
                hex.append(String.format("%02x", _byte));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e); // TODO: CREAR EXCEPCIÓN PERSONALIZADA (TokenHashingException).
        }
    }

}
