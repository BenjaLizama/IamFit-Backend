package com.iamfit.ejercicios.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReorderExerciseRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Debería fallar si faltan los campos requeridos")
    void invalidRequest() {
        ReorderExerciseRequest request = new ReorderExerciseRequest();

        Set<ConstraintViolation<ReorderExerciseRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size(), "Debe fallar porque ambos campos son nulos");
    }

    @Test
    @DisplayName("Debería pasar con todos los campos correctos")
    void validRequest() {
        ReorderExerciseRequest request = new ReorderExerciseRequest();
        request.setExerciseEntryId(UUID.randomUUID());
        request.setNewOrderIndex(1);

        Set<ConstraintViolation<ReorderExerciseRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}