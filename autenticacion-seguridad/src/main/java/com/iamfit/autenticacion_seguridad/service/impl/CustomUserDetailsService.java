package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CredentialRepository credentialRepository;

    @Override
    @NullMarked
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        CredentialEntity credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontraron credenciales para el email: " + email));
        return new SecurityCredential(credential);
    }
}
