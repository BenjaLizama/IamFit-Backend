package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeneratedRoutinesResponseTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        GeneratedRoutinesResponse response = GeneratedRoutinesResponse.builder()
                .sessionId("session-xyz")
                .message("Éxito")
                .routines(new ArrayList<>())
                .build();

        assertNotNull(response);
        assertEquals("session-xyz", response.getSessionId());
        assertEquals("Éxito", response.getMessage());
        assertNotNull(response.getRoutines());
    }
}
