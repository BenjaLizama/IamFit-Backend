package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.dto.LoginRequest;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;
import com.iamfit.autenticacion_seguridad.exception.UnsupportedAuthenticationProviderException;
import com.iamfit.autenticacion_seguridad.service.strategy.AuthenticationStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthStrategyManager {

    private final List<AuthenticationStrategy> strategyList;
    private Map<LoginProvider, AuthenticationStrategy> strategies;

    @PostConstruct
    public void init() {
        // Mapeamos cada estrategia a su enumerador para acceso O(1)
        strategies = strategyList.stream()
                .collect(Collectors.toMap(AuthenticationStrategy::getProvider, s -> s));
    }

    public CredentialEntity executeStrategy(LoginRequest request) {
        AuthenticationStrategy strategy = strategies.get(request.provider());
        if (strategy == null) {
            throw new UnsupportedAuthenticationProviderException("Proveedor de login no soportado: " + request.provider());
        }
        return strategy.authenticate(request);
    }
}
