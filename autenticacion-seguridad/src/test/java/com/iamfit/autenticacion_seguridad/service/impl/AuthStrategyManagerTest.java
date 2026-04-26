package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.LoginRequest;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.exception.UnsupportedAuthenticationProviderException;
import com.iamfit.autenticacion_seguridad.service.strategy.AuthenticationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthStrategyManagerTest {

    @Mock
    private AuthenticationStrategy localStrategy;

    @Mock
    private AuthenticationStrategy googleStrategy;

    private AuthStrategyManager authStrategyManager;

    @BeforeEach
    void setUp() {
        when(localStrategy.getProvider()).thenReturn(LoginProvider.LOCAL);
        when(googleStrategy.getProvider()).thenReturn(LoginProvider.GOOGLE);

        List<AuthenticationStrategy> strategyList = List.of(localStrategy, googleStrategy);

        authStrategyManager = new AuthStrategyManager(strategyList);

        authStrategyManager.init();
    }

    @Test
    @DisplayName("Debería ejecutar la estrategia correcta según el proveedor")
    void executeStrategy_Success() {
        LoginRequest request = new LoginRequest("test@mail.com", "123", LoginProvider.LOCAL);
        CredentialEntity mockCredential = new CredentialEntity();

        when(localStrategy.authenticate(request)).thenReturn(mockCredential);

        CredentialEntity result = authStrategyManager.executeStrategy(request);

        assertNotNull(result);
        verify(localStrategy, times(1)).authenticate(request);
        verify(googleStrategy, never()).authenticate(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si el proveedor no está en el mapa")
    void executeStrategy_Fail_UnsupportedProvider() {
        LoginRequest request = new LoginRequest("test@mail.com", "123", LoginProvider.GITHUB);

        UnsupportedAuthenticationProviderException exception = assertThrows(
                UnsupportedAuthenticationProviderException.class,
                () -> authStrategyManager.executeStrategy(request)
        );

        assertTrue(exception.getMessage().contains("GITHUB"));
    }
}
