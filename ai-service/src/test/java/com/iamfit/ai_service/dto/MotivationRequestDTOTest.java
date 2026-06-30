package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MotivationRequestDTOTest {

    @Test
    @DisplayName("MotivationRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testMotivationRequestDTO() {
        MotivationRequestDTO dto = new MotivationRequestDTO();
        dto.setContexto("antes de entrenar");

        assertEquals("antes de entrenar", dto.getContexto());
        assertNotNull(dto.toString());
    }
}