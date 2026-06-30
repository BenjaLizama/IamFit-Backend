package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AIResponseDTOTest {

    @Test
    @DisplayName("AIResponseDTO - Debe funcionar correctamente con Getter, Setter y Builder")
    void testAIResponseDTO() {
        MiaAction action = MiaAction.builder().type("TEST").label("Test").build();
        List<MiaAction> actions = List.of(action);

        AIResponseDTO dto = AIResponseDTO.builder()
                .content("Hola")
                .model("gemini")
                .status("SUCCESS")
                .disclaimer("Aviso")
                .actions(actions)
                .build();

        assertEquals("Hola", dto.getContent());
        assertEquals("gemini", dto.getModel());
        assertEquals("SUCCESS", dto.getStatus());
        assertEquals("Aviso", dto.getDisclaimer());
        assertEquals(actions, dto.getActions());

        dto.setContent("Nuevo Contenido");
        assertEquals("Nuevo Contenido", dto.getContent());
        assertNotNull(dto.toString());
    }
}