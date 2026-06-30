package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeeklyMenuResponseDTOTest {

    @Test
    @DisplayName("WeeklyMenuResponseDTO - Debe funcionar correctamente con Getter y Builder")
    void testWeeklyMenuResponseDTO() {
        WeeklyMenuResponseDTO dto = WeeklyMenuResponseDTO.builder()
                .userId("USER_A1")
                .menu("Lunes: Pollo con arroz...")
                .objetivo("mantener")
                .calorias(2000)
                .status("SUCCESS")
                .disclaimer("Consulta a tu nutricionista")
                .build();

        assertEquals("USER_A1", dto.getUserId());
        assertEquals("Lunes: Pollo con arroz...", dto.getMenu());
        assertEquals("mantener", dto.getObjetivo());
        assertEquals(2000, dto.getCalorias());
        assertEquals("SUCCESS", dto.getStatus());
        assertEquals("Consulta a tu nutricionista", dto.getDisclaimer());
        assertNotNull(dto.toString());
    }
}