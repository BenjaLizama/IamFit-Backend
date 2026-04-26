package com.iamfit.autenticacion_seguridad.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public interface ITokenHashService {
    String hash(String token);
}
