package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.repository.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCleanupServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionCleanupService sessionCleanupService;

    @Test
    @DisplayName("Debería invocar la eliminación de sesiones expiradas usando el tiempo actual")
    void cleanExpiredTokens_ShouldInvokeRepository() {
        sessionCleanupService.cleanExpiredTokens();

        verify(sessionRepository, times(1)).deleteByExpiryDateBefore(any(Instant.class));
    }

    @Test
    @DisplayName("Debería verificar que el Instant enviado no es futuro")
    void cleanExpiredTokens_VerifyTimeIsCoherent() {
        sessionCleanupService.cleanExpiredTokens();

        verify(sessionRepository).deleteByExpiryDateBefore(argThat(instant ->
                instant.isBefore(Instant.now().plusMillis(100))
        ));
    }
}
