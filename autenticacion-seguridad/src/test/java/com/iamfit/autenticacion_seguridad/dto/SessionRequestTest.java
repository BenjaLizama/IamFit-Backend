package com.iamfit.autenticacion_seguridad.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionRequestTest {

    @Test
    @DisplayName("Debería crear SessionRequest correctamente con los campos definidos")
    void shouldCreateSessionRequest() {
        // Arrange
        String deviceId = "UUID-12345";
        String deviceName = "Samsung Galaxy S23";

        // Act
        SessionRequest request = new SessionRequest(deviceId, deviceName);

        // Assert
        assertNotNull(request);
        assertEquals(deviceId, request.deviceId());
        assertEquals(deviceName, request.deviceName());
        // Eliminamos la referencia a ua() porque no existe en tu Record
    }

    @Test
    @DisplayName("Prueba de igualdad entre instancias (equals y hashCode)")
    void testEqualsAndHashCode() {
        SessionRequest request1 = new SessionRequest("id-1", "PC-Oficina");
        SessionRequest request2 = new SessionRequest("id-1", "PC-Oficina");
        SessionRequest request3 = new SessionRequest("id-2", "PC-Casa");

        assertEquals(request1, request2, "Instancias con mismos datos deben ser iguales");
        assertNotEquals(request1, request3, "Instancias con distintos datos no deben ser iguales");
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Prueba de representación en cadena (toString)")
    void testToString() {
        SessionRequest request = new SessionRequest("dev-id", "dev-name");
        String result = request.toString();

        assertNotNull(result);
        assertTrue(result.contains("deviceId=dev-id"));
        assertTrue(result.contains("deviceName=dev-name"));
        // Aquí también eliminamos ua-string
    }
}