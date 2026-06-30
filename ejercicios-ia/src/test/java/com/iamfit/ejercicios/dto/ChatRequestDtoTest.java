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

class ChatRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Debería fallar si la pregunta está en blanco o es nula")
    void invalidChatRequest() {
        ChatRequestDto requestVacio = new ChatRequestDto("   ");
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(requestVacio);

        assertFalse(violations.isEmpty());
        assertEquals("la pregunta no puede esta vacia", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Debería pasar si la pregunta es válida")
    void validChatRequest() {
        ChatRequestDto requestValido = new ChatRequestDto("¿Cómo hago una sentadilla?");
        Set<ConstraintViolation<ChatRequestDto>> violations = validator.validate(requestValido);

        assertTrue(violations.isEmpty());
    }
}