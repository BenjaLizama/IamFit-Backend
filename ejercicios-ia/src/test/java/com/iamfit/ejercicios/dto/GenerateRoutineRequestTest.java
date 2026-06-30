package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenerateRoutineRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Debería fallar si faltan valores requeridos (@NotNull)")
    void invalidRequest() {
        GenerateRoutineRequest request = new GenerateRoutineRequest();
        // Faltan difficulty y muscleGroups

        Set<ConstraintViolation<GenerateRoutineRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Debería pasar si tiene todos los campos requeridos")
    void validRequest() {
        GenerateRoutineRequest request = new GenerateRoutineRequest();
        request.setDifficulty(DifficultyLevel.PRINCIPIANTE); // O la enumeración que corresponda
        request.setMuscleGroups(List.of(MuscleGroup.PECHO)); // O la enumeración que corresponda

        Set<ConstraintViolation<GenerateRoutineRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}