package com.iamfit.autenticacion_seguridad.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ClientContextHolderTest {

    @AfterEach
    void tearDown() {
        ClientContextHolder.clear();
    }

    @Test
    @DisplayName("Debería guardar y recuperar metadatos incluyendo el Device ID")
    void setAndGet_ShouldReturnCorrectData() {
        // Arrange
        String ip = "192.168.1.1";
        String ua = "Mozilla/5.0";
        String token = "jwt-secret-token";
        String deviceId = "iphone-15-pro"; // El nuevo campo

        // Act
        ClientContextHolder.setContext(ip, ua, token, deviceId);

        // Assert
        assertThat(ClientContextHolder.getIp()).isEqualTo(ip);
        assertThat(ClientContextHolder.getUserAgent()).isEqualTo(ua);
        assertThat(ClientContextHolder.getToken()).isEqualTo(token);
        assertThat(ClientContextHolder.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("Debería retornar null si no hay contexto establecido")
    void get_ShouldReturnNullWhenEmpty() {
        assertThat(ClientContextHolder.getIp()).isNull();
        assertThat(ClientContextHolder.getUserAgent()).isNull();
        assertThat(ClientContextHolder.getToken()).isNull();
        assertThat(ClientContextHolder.getDeviceId()).isNull();
    }

    @Test
    @DisplayName("Debería limpiar el contexto correctamente")
    void clear_ShouldRemoveMetadata() {
        // Arrange
        ClientContextHolder.setContext("127.0.0.1", "Postman", "X", "dev-123");

        // Act
        ClientContextHolder.clear();

        // Assert
        assertThat(ClientContextHolder.getIp()).isNull();
        assertThat(ClientContextHolder.getDeviceId()).isNull();
        assertThat(ClientContextHolder.getToken()).isNull();
    }

    @Test
    @DisplayName("Debería mantener el aislamiento entre diferentes hilos (ThreadLocal)")
    void threadIsolation_ShouldNotLeakDataBetweenThreads() throws InterruptedException {
        // Hilo Principal (Thread A)
        ClientContextHolder.setContext("1.1.1.1", "Thread-A", "token-A", "device-A");

        AtomicReference<String> ipInThreadB = new AtomicReference<>();
        AtomicReference<String> deviceIdInThreadB = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Hilo secundario (Thread B)
        Thread threadB = new Thread(() -> {
            ipInThreadB.set(ClientContextHolder.getIp());
            deviceIdInThreadB.set(ClientContextHolder.getDeviceId());

            // Establecemos datos propios en B
            ClientContextHolder.setContext("2.2.2.2", "Thread-B", "token-B", "device-B");
            latch.countDown();
        });

        threadB.start();
        latch.await(1, TimeUnit.SECONDS);

        // Verificaciones
        assertThat(ipInThreadB.get()).as("El Hilo B no debería ver la IP del Hilo A").isNull();
        assertThat(deviceIdInThreadB.get()).as("El Hilo B no debería ver el Device ID del Hilo A").isNull();

        assertThat(ClientContextHolder.getIp())
                .as("El Hilo A mantiene su propia IP")
                .isEqualTo("1.1.1.1");
        assertThat(ClientContextHolder.getDeviceId())
                .as("El Hilo A mantiene su propio Device ID")
                .isEqualTo("device-A");
    }
}
