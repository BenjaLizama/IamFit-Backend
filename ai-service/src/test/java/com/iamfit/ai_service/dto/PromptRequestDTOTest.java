package com.iamfit.ai_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PromptRequestDTOTest {

    private final Validator validator;

    PromptRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("PromptRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testPromptRequestDTO() {
        PromptRequestDTO dto = new PromptRequestDTO();
        dto.setMessage("Mensaje de prueba");

        assertEquals("Mensaje de prueba", dto.getMessage());
        assertNotNull(dto.toString());
    }

    @Test
    @DisplayName("PromptRequestDTO - Debe fallar validación si el mensaje está en blanco")
    void testValidationBlankMessage() {
        PromptRequestDTO dto = new PromptRequestDTO();
        dto.setMessage("");

        Set<ConstraintViolation<PromptRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}