package com.iamfit.autenticacion_seguridad.service.strategy;

import com.iamfit.autenticacion_seguridad.dto.LoginRequest;
import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.enums.LoginProvider;

public interface AuthenticationStrategy {
    LoginProvider getProvider();
    CredentialEntity authenticate(LoginRequest loginRequest);
}
