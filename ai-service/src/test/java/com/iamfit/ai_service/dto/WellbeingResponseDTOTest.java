package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WellbeingResponseDTOTest {

    @Test
    @DisplayName("WellbeingResponseDTO - Debe funcionar correctamente con Getter y Builder")
    void testWellbeingResponseDTO() {
        WellbeingResponseDTO dto = WellbeingResponseDTO.builder()
                .response("Sigue así, estás manejando bien el estrés.")
                .tipo("CHECK_IN")
                .status("SUCCESS")
                .disclaimer("Uso meramente informativo")
                .build();

        assertEquals("Sigue así, estás manejando bien el estrés.", dto.getResponse());
        assertEquals("CHECK_IN", dto.getTipo());
        assertEquals("SUCCESS", dto.getStatus());
        assertEquals("Uso meramente informativo", dto.getDisclaimer());
        assertNotNull(dto.toString());
    }
}