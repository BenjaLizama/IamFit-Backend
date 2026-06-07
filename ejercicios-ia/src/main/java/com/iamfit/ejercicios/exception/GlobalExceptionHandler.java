package com.iamfit.ejercicios.exception;

import com.iamfit.ejercicios.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
                500, "SYS_500",
                "Error interno del servidor.",
                request.getRequestURI()));
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
}