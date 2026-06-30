package com.iamfit.ejercicios.exception;

import com.iamfit.ejercicios.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}: ", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
                500, "SYS_500", "Error interno del servidor.", request.getRequestURI()));
    }

    @ExceptionHandler(RoutineSessionExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleSessionExpired(
            RoutineSessionExpiredException ex, HttpServletRequest request) {
        return ResponseEntity.status(410).body(buildError(
                410, "ROUTINE_SESSION_EXPIRED", ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(RoutineLimitReachedException.class)
    public ResponseEntity<Map<String, Object>> handleLimitReached(
            RoutineLimitReachedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError(
                409, "ROUTINE_LIMIT_REACHED", ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildError(
                400, "VALIDATION_ERROR", ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> body = buildError(400, "ERR_VALIDATION_001",
                "Por favor verifica los datos ingresados.", request.getRequestURI());
        body.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    private Map<String, Object> buildError(int status, String code,
                                           String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("code", code);
        body.put("message", message);
        body.put("path", path);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }

    @ExceptionHandler(WorkoutSessionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSessionNotFound(
            WorkoutSessionNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404).body(buildError(
                404, "WORKOUT_SESSION_NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ExerciseAlreadyCompletedException.class)
    public ResponseEntity<Map<String, Object>> handleExerciseAlreadyCompleted(
            ExerciseAlreadyCompletedException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(buildError(
                409, "EXERCISE_ALREADY_COMPLETED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ExerciseNotCompletedException.class)
    public ResponseEntity<Map<String, Object>> handleExerciseNotCompleted(
            ExerciseNotCompletedException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(buildError(
                409, "EXERCISE_NOT_COMPLETED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(RoutineNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleRoutineNotActive(
            RoutineNotActiveException ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(buildError(
                409, "ROUTINE_NOT_ACTIVE", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.core.convert.ConversionFailedException.class)
    public ResponseEntity<Map<String, Object>> handleConversionFailed(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(400).body(buildError(
                400, "ERR_VALIDATION_001", "El identificador proporcionado no es valido.",
                request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(400).body(buildError(
                400, "ERR_VALIDATION_001", "El identificador proporcionado no es valido.",
                request.getRequestURI()));
    }
}