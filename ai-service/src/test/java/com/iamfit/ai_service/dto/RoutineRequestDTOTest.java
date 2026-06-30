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

class RoutineRequestDTOTest {

    private final Validator validator;

    RoutineRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("RoutineRequestDTO - Debe funcionar correctamente con Getter y Setter")
    void testRoutineRequestDTO() {
        RoutineRequestDTO dto = new RoutineRequestDTO();
        dto.setObjetivo("hipertrofia");
        dto.setDiasDisponibles(4);
        dto.setNivel("avanzado");
        dto.setLesiones(List.of("Hombro"));

        assertEquals("hipertrofia", dto.getObjetivo());
        assertEquals(4, dto.getDiasDisponibles());
        assertEquals("avanzado", dto.getNivel());
        assertEquals("Hombro", dto.getLesiones().get(0));
        assertNotNull(dto.toString());
    }

    @Test
    @DisplayName("RoutineRequestDTO - Debe fallar si los días disponibles superan el límite")
    void testRoutineRequestDTOValidation() {
        RoutineRequestDTO dto = new RoutineRequestDTO();
        dto.setObjetivo("");
        dto.setDiasDisponibles(8); // Máximo permitido es 7
        dto.setNivel("Principiante");

        Set<ConstraintViolation<RoutineRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}