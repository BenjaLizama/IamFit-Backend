package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoutineResponseDTOTest {

    @Test
    @DisplayName("RoutineResponseDTO - Debe funcionar correctamente con Getter y Builder")
    void testRoutineResponseDTO() {
        RoutineResponseDTO dto = RoutineResponseDTO.builder()
                .userId("USER99")
                .rutina("Push/Pull/Legs")
                .objetivo("fuerza")
                .nivel("intermedio")
                .diasDisponibles(3)
                .status("ACTIVE")
                .disclaimer("Cuidado")
                .build();

        assertEquals("USER99", dto.getUserId());
        assertEquals("Push/Pull/Legs", dto.getRutina());
        assertEquals("fuerza", dto.getObjetivo());
        assertEquals("intermedio", dto.getNivel());
        assertEquals(3, dto.getDiasDisponibles());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("Cuidado", dto.getDisclaimer());
        assertNotNull(dto.toString());
    }
}