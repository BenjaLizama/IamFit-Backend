package com.iamfit.ejercicios.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutineLimitsDtoTest {

    @Test
    @DisplayName("Debería construirse correctamente mediante su Builder")
    void builderCreation() {
        RoutineLimitsDto limits = RoutineLimitsDto.builder()
                .maxActiveRoutines(3)
                .activeRoutines(1L)
                .inactiveRoutines(5L)
                .canCreateRoutine(true)
                .build();

        assertNotNull(limits);
        assertEquals(3, limits.getMaxActiveRoutines());
        assertEquals(1L, limits.getActiveRoutines());
        assertEquals(5L, limits.getInactiveRoutines());
        assertTrue(limits.getCanCreateRoutine());
    }
}