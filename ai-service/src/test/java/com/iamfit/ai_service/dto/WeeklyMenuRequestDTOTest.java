package com.iamfit.ai_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WeeklyMenuRequestDTOTest {

    private final Validator validator;

    WeeklyMenuRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("WeeklyMenuRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testWeeklyMenuRequestDTO() {
        WeeklyMenuRequestDTO dto = new WeeklyMenuRequestDTO();
        dto.setObjetivo("bajar");
        dto.setCalorias(2200);
        dto.setAlergias(List.of("Maní", "Lactosa"));

        assertEquals("bajar", dto.getObjetivo());
        assertEquals(2200, dto.getCalorias());
        assertEquals("Maní", dto.getAlergias().get(0));
        assertNotNull(dto.toString());
    }

    @Test
    @DisplayName("WeeklyMenuRequestDTO - Debe fallar si el objetivo está vacío o las calorías están fuera de rango")
    void testWeeklyMenuRequestDTOValidation() {
        WeeklyMenuRequestDTO dto = new WeeklyMenuRequestDTO();
        dto.setObjetivo(""); // Inválido: @NotBlank
        dto.setCalorias(600); // Inválido: @Min(1000)

        Set<ConstraintViolation<WeeklyMenuRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("El objetivo no puede estar vacío")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Mínimo 1000 kcal")));
    }
}