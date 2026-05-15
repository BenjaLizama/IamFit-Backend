package com.iamfit.autenticacion_seguridad.service.strategy;

import com.iamfit.autenticacion_seguridad.dto.LoginRequest;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordAuthStrategy implements AuthenticationStrategy {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginProvider getProvider() {
        return LoginProvider.LOCAL;
    }

    @Override
    public CredentialEntity authenticate(LoginRequest loginRequest) {
        // 1. Buscar la credencial por el identifier (email en este caso)
        CredentialEntity credential = credentialRepository.findByEmail(loginRequest.identifier())
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));

        // 2. Validar si la cuenta está activa
        if (!credential.getIsActive()) {
            throw new DisabledException("La cuenta se encuentra desactivada.");
        }

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(loginRequest.password(), credential.getPassword())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }

        return credential;
    }
}
