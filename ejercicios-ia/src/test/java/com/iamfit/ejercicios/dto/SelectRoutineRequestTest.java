package com.iamfit.ejercicios.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SelectRoutineRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Debería pasar la validación con datos correctos")
    void validRequest() {
        SelectRoutineRequest request = new SelectRoutineRequest();
        request.setSessionId("session-123");
        request.setSelectedIndex(1); // Dentro de [0, 2]
        request.setCustomName("Mi Rutina Favorita");

        Set<ConstraintViolation<SelectRoutineRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "No debería haber errores");
    }

    @Test
    @DisplayName("Debería fallar si los campos requeridos son nulos")
    void invalidRequest_NullFields() {
        SelectRoutineRequest request = new SelectRoutineRequest();
        // sessionId y selectedIndex son nulos

        Set<ConstraintViolation<SelectRoutineRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Debería fallar si el selectedIndex es menor a 0 o mayor a 2")
    void invalidRequest_OutOfBounds() {
        SelectRoutineRequest requestMenor = new SelectRoutineRequest();
        requestMenor.setSessionId("session-123");
        requestMenor.setSelectedIndex(-1); // Falla por @Min(0)

        SelectRoutineRequest requestMayor = new SelectRoutineRequest();
        requestMayor.setSessionId("session-123");
        requestMayor.setSelectedIndex(3); // Falla por @Max(2)

        Set<ConstraintViolation<SelectRoutineRequest>> violationsMenor = validator.validate(requestMenor);
        Set<ConstraintViolation<SelectRoutineRequest>> violationsMayor = validator.validate(requestMayor);

        assertEquals(1, violationsMenor.size());
        assertEquals(1, violationsMayor.size());
    }
}