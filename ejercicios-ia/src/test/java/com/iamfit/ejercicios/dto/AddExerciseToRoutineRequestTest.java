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

class AddExerciseToRoutineRequestTest {

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
        AddExerciseToRoutineRequest request = new AddExerciseToRoutineRequest();
        request.setExerciseId(UUID.randomUUID());
        request.setSets(3);
        request.setReps(10);

        Set<ConstraintViolation<AddExerciseToRoutineRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "No debería haber errores");
    }

    @Test
    @DisplayName("Debería fallar si los valores requeridos son nulos o negativos")
    void invalidRequest() {
        AddExerciseToRoutineRequest request = new AddExerciseToRoutineRequest();
        request.setExerciseId(null);
        request.setSets(0); // Falla @Positive
        request.setReps(-5); // Falla @Positive

        Set<ConstraintViolation<AddExerciseToRoutineRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
    }
}