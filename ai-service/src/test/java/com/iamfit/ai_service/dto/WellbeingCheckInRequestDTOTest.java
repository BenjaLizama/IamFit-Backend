package com.iamfit.ai_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WellbeingCheckInRequestDTOTest {

    private final Validator validator;

    WellbeingCheckInRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("WellbeingCheckInRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testWellbeingCheckInRequestDTO() {
        WellbeingCheckInRequestDTO dto = new WellbeingCheckInRequestDTO();
        dto.setEstadoAnimo(4);
        dto.setNivelEstres(2);
        dto.setNota("Me sentí muy bien hoy entrenando.");

        assertEquals(4, dto.getEstadoAnimo());
        assertEquals(2, dto.getNivelEstres());
        assertEquals("Me sentí muy bien hoy entrenando.", dto.getNota());
        assertNotNull(dto.toString());
    }

    @Test
    @DisplayName("WellbeingCheckInRequestDTO - Debe fallar si los valores numéricos superan el límite")
    void testWellbeingCheckInRequestDTOValidation() {
        WellbeingCheckInRequestDTO dto = new WellbeingCheckInRequestDTO();
        dto.setEstadoAnimo(6); // Inválido: @Max(5)
        dto.setNivelEstres(0); // Inválido: @Min(1)

        Set<ConstraintViolation<WellbeingCheckInRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("El estado de ánimo debe ser entre 1 y 5")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("El nivel de estrés debe ser entre 1 y 5")));
    }
}