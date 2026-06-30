package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MiaActionTest {

    @Test
    @DisplayName("MiaAction - Debe funcionar correctamente con Getter y Builder")
    void testMiaAction() {
        Object payloadObj = new Object();
        MiaAction dto = MiaAction.builder()
                .type("REDIRECT")
                .label("Ir a perfil")
                .payload(payloadObj)
                .build();

        assertEquals("REDIRECT", dto.getType());
        assertEquals("Ir a perfil", dto.getLabel());
        assertEquals(payloadObj, dto.getPayload());
        assertNotNull(dto.toString());
    }
}