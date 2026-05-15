package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Debería retornar UserDetails cuando el email existe")
    void loadUserByUsername_Success() {
        String email = "test@nomall.com";
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID());
        credential.setEmail(email);
        credential.setPassword("hashed_password");
        credential.setIsActive(true);

        when(credentialRepository.findByEmail(email)).thenReturn(Optional.of(credential));

        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertTrue(result instanceof SecurityCredential);
        verify(credentialRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Debería lanzar UsernameNotFoundException cuando el email no existe")
    void loadUserByUsername_UserNotFound() {
        String email = "notfound@nomall.com";
        when(credentialRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email)
        );

        assertTrue(exception.getMessage().contains(email));
        verify(credentialRepository, times(1)).findByEmail(email);
    }
}
