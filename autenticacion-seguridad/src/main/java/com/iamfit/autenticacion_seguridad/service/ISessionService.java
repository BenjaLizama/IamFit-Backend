package com.iamfit.autenticacion_seguridad.service;

import com.iamfit.autenticacion_seguridad.dto.*;

import java.util.UUID;

public interface ISessionService {

    SessionResponse createSession(UUID credentialId, SessionRequest sessionRequest);
    AuthResponse refreshToken(RefreshTokenWrapper wrapper);
    AuthResponse changePassword(ChangePasswordRequest request);
    DeactivateResponse deactivateSelf();
    void logout();

}
