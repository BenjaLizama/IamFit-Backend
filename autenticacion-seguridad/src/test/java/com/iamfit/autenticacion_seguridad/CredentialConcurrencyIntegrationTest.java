package com.iamfit.autenticacion_seguridad;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.repository.CredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CredentialConcurrencyIntegrationTest {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UUID credentialId;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            registry.add("RSA_PRIVATE", () -> Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));
            registry.add("RSA_PUBLIC", () -> Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
            registry.add("DEV_PORT", () -> "0");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();

        CredentialEntity credential = new CredentialEntity();
        credential.setEmail("concurrency-" + UUID.randomUUID() + "@test.com");
        credential.setPassword("encoded-password");
        credential.setIsActive(true);

        credentialId = credentialRepository.save(credential).getId();
    }

    @Test
    @DisplayName("Debería detectar conflicto optimista cuando dos transacciones modifican la misma credencial")
    void changePassword_ConcurrencyConflict() throws InterruptedException {
        // CountDownLatch sincroniza los dos hilos para que lean al mismo tiempo
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 2; i++) {
            final String newPassword = "nueva-pass-" + i;
            executor.submit(() -> {
                try {
                    // Ambos hilos esperan aquí hasta que startLatch llegue a 0
                    startLatch.await();

                    transactionTemplate.execute(status -> {
                        CredentialEntity credential = credentialRepository.findById(credentialId)
                                .orElseThrow();
                        credential.setPassword(newPassword);
                        credentialRepository.saveAndFlush(credential);
                        return null;
                    });

                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Libera ambos hilos al mismo tiempo
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Exactamente uno debe ganar y uno debe fallar
        assertEquals(1, successCount.get(), "Solo una transacción debería tener éxito");
        assertEquals(1, conflictCount.get(), "Solo una transacción debería fallar por conflicto");
    }
}