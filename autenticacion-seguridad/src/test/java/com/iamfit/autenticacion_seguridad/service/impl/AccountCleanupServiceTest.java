package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCleanupServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private AccountCleanupService accountCleanupService;

    @Test
    @DisplayName("Debería llamar al repositorio y reportar cuentas eliminadas cuando existan")
    void deleteInactiveAccounts_Success_WithDeletedAccounts() {
        when(credentialRepository.deleteOldInactiveAccounts(any(Instant.class))).thenReturn(5);

        accountCleanupService.deleteInactiveAccounts();

        verify(credentialRepository, times(1)).deleteOldInactiveAccounts(any(Instant.class));
    }

    @Test
    @DisplayName("Debería funcionar correctamente incluso si no hay cuentas para eliminar")
    void deleteInactiveAccounts_Success_NoAccounts() {
        when(credentialRepository.deleteOldInactiveAccounts(any(Instant.class))).thenReturn(0);

        accountCleanupService.deleteInactiveAccounts();

        verify(credentialRepository, times(1)).deleteOldInactiveAccounts(any(Instant.class));
    }

    // @BeforeEach para inyectar manualmente el valor de gracePeriodDays, pq antes mockito con @InjectMocks no procesaba los @Value
    // Eso hacía que el campo me quedara en 0 y el cutoff no era 30 días

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountCleanupService, "gracePeriodDays", 30);
    }

    @Test
    @DisplayName("Debería verificar que el cutoff enviado es aproximadamente 30 días atrás")
    void deleteInactiveAccounts_VerifyCutoff() {
        when(credentialRepository.deleteOldInactiveAccounts(any(Instant.class))).thenReturn(0);

        Instant before = Instant.now();
        accountCleanupService.deleteInactiveAccounts();
        Instant after = Instant.now();

        verify(credentialRepository).deleteOldInactiveAccounts(argThat(instant ->
                !instant.isBefore(before.minus(30, java.time.temporal.ChronoUnit.DAYS).minusSeconds(1)) &&
                        !instant.isAfter(after.minus(30, java.time.temporal.ChronoUnit.DAYS).plusSeconds(1))
        ));
    }
}
