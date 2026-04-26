package com.iamfit.autenticacion_seguridad.service;

public interface ITokenCleanupService {
    void cleanExpiredTokens();
}
