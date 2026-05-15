package com.iamfit.autenticacion_seguridad.service;

import com.iamfit.autenticacion_seguridad.dto.AuthResponse;
import com.iamfit.autenticacion_seguridad.dto.LoginWrapper;
import com.iamfit.autenticacion_seguridad.dto.RegisterWrapper;

public interface IAuthService {
    AuthResponse login(LoginWrapper wrapper);
    AuthResponse register(RegisterWrapper registerWrapper);
}