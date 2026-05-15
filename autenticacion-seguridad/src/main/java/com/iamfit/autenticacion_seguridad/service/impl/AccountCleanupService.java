package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCleanupService {

    private final CredentialRepository credentialRepository;

    @Value("${app.cleanup.account.grace-period-days:30}")
    private int gracePeriodDays;

    @Scheduled(cron = "${app.cleanup.account.cron:0 0 3 * * ?}")
    @Transactional
    public void deleteInactiveAccounts() {
        // punto corte 30 días atrás
        Instant cutoff = Instant.now().minus(gracePeriodDays, ChronoUnit.DAYS);

        // Seguridad: solo eliminar cuentas que tienen deactivatedAt registrado
        // para no borrar cuentas con isActive=false pero sin fecha de desactivación.
        int deleted = credentialRepository.deleteOldInactiveAccounts(cutoff);

        if (deleted > 0) {
            log.info("[ACCOUNT-CLEANUP] Limpieza exitosa. Cuentas eliminadas por inactividad (>30 días): {}", deleted);
        } else {
            log.debug("[ACCOUNT-CLEANUP] No se encontraron cuentas inactivas para eliminar.");
        }
    }
}