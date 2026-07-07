package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechniqueRequestDTOTest {

    @Test
    @DisplayName("TechniqueRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testTechniqueRequestDTO() {
        TechniqueRequestDTO dto = new TechniqueRequestDTO();
        dto.setTipo("respiracion");

        assertEquals("respiracion", dto.getTipo());
        assertNotNull(dto.toString());
    }
}