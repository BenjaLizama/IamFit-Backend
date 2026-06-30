package com.iamfit.alimentacion.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles validation errors (@Valid on the request body). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Datos de entrada inválidos");
        problem.setProperty("code", "ERR_VALIDATION_001");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Handles AI generation or JSON parsing failures. */
    @ExceptionHandler(MealPlanGenerationException.class)
    public ProblemDetail handleMealPlanGeneration(MealPlanGenerationException ex) {
        log.error("MealPlanGenerationException: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("Error al generar el plan de comidas");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MealAlreadyConsumedException.class)
    public ProblemDetail handleAlreadyConsumed(MealAlreadyConsumedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Comida ya consumida");
        problem.setProperty("code", "MEAL_ALREADY_CONSUMED");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MealNotConsumedException.class)
    public ProblemDetail handleNotConsumed(MealNotConsumedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Comida no consumida");
        problem.setProperty("code", "MEAL_NOT_CONSUMED");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MealPlanNotActiveException.class)
    public ProblemDetail handleNotActive(MealPlanNotActiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Plan de comidas no activo");
        problem.setProperty("code", "MEAL_PLAN_NOT_ACTIVE");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MealPlanDayNotFoundException.class)
    public ProblemDetail handleDayNotFound(MealPlanDayNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Dia del plan no encontrado");
        problem.setProperty("code", "MEAL_PLAN_DAY_NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Catch-all for unexpected exceptions. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        problem.setTitle("Error inesperado");
        problem.setProperty("code", "SYS_500");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}