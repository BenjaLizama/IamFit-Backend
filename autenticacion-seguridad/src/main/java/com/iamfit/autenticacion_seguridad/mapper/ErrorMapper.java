package com.iamfit.autenticacion_seguridad.mapper;

import com.iamfit.autenticacion_seguridad.dto.StandarErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorMapper {

    /**
     * BASE PARA EL RESTO DE MAPPERS
     */
    private StandarErrorResponse.StandarErrorResponseBuilder buildBaseError(HttpStatus status, String path) {
        return StandarErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .path(path)
                .timestamp(System.currentTimeMillis());
    }


    // ==========================================
    //       ERROR: 400 (BAD REQUEST)
    // ==========================================

    public StandarErrorResponse toValidationResponse(MethodArgumentNotValidException ex, String path) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return buildBaseError(HttpStatus.BAD_REQUEST, path)
                .code("ERR_VALIDATION_001")
                .message("Por favor, verifica los datos ingresados.")
                .developerMessage("La validación del DTO falló en uno o más campos.")
                .validationError(errors)
                .build();
    }

    public StandarErrorResponse toBusinessResponse(IllegalArgumentException ex, String path) {
        return buildBaseError(HttpStatus.BAD_REQUEST, path)
                .code("ERR_BUSINESS_001")
                .message(ex.getMessage())
                .developerMessage("Se recibió un argumento inválido que viola las reglas de negocio.")
                .build();
    }

    public StandarErrorResponse toMalformedRequestResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.BAD_REQUEST, path)
                .code("ERR_MALFORMED_001")
                .message("El formato de la petición es inválido. Revisa la estructura de los datos.")
                .developerMessage(ex.getMessage())
                .build();
    }

    public StandarErrorResponse toUnsupportedProviderResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.BAD_REQUEST, path)
                .code("AUTH_PROVIDER_001")
                .message("El proveedor de autenticación no es válido o no está soportado.")
                .developerMessage(ex.getMessage())
                .build();
    }


    // ==========================================
    //       ERROR: 401 (UNAUTHORIZED)
    // ==========================================

    public StandarErrorResponse toAuthResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.UNAUTHORIZED, path)
                .code("AUTH_001")
                .message("Correo o contraseña incorrectos.")
                .developerMessage(ex.getMessage())
                .build();
    }

    public StandarErrorResponse toRefreshTokenNotFound(Exception ex, String path) {
        return buildBaseError(HttpStatus.UNAUTHORIZED, path)
                .code("AUTH_002")
                .message("Sesión inválida o expirada.")
                .developerMessage(ex.getMessage())
                .build();
    }


    // ==========================================
    //         ERROR: 403 (FORBIDDEN)
    // ==========================================

    public StandarErrorResponse toAccessDeniedResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.FORBIDDEN, path)
                .code("ERR_FORBIDDEN_001")
                .message("No tienes los permisos necesarios para realizar esta acción.")
                .developerMessage(ex.getMessage())
                .build();
    }

    public StandarErrorResponse toAccountDisabledResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.FORBIDDEN, path)
                .code("ERR_FORBIDDEN_002")
                .message("La cuenta se encuentra deshabilitada.")
                .developerMessage(ex.getMessage())
                .build();
    }

    public StandarErrorResponse toSecurityViolationResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.FORBIDDEN, path)
                .code("ERR_SECURITY_002")
                .message("Esta sesión no pertenece a este dispositivo.")
                .developerMessage(ex.getMessage())
                .build();
    }


    // ==========================================
    //     ERROR: 405 (METHOD NOT ALLOWED)
    // ==========================================

    public StandarErrorResponse toMethodNotSupportedResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.METHOD_NOT_ALLOWED, path)
                .code("ERR_METHOD_001")
                .message("El método HTTP utilizado no está soportado para esta ruta.")
                .developerMessage(ex.getMessage())
                .build();
    }


    // ==========================================
    //         ERROR: 409 (CONFLICT)
    // ==========================================

    public StandarErrorResponse toConcurrencyConflictResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.CONFLICT, path)
                .code("ERR_CONCURRENCY_001")
                .message("La operación no pudo completarse porque otro proceso modificó los mismos datos. Por favor, intenta nuevamente.")
                .developerMessage("OptimisticLockException: " + ex.getMessage())
                .build();
    }


    // ==========================================
    //    ERROR: 422 (UNPROCESSABLE ENTITY)
    // ==========================================

    public StandarErrorResponse toStrategyNotImplementedResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.UNPROCESSABLE_CONTENT, path)
                .code("AUTH_STRATEGY_001")
                .message("Este método de inicio de sesión aún no está disponible.")
                .developerMessage("Se encontró el proveedor pero no hay una estrategia activa: " + ex.getMessage())
                .build();
    }

    public StandarErrorResponse toInvalidPasswordResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.UNPROCESSABLE_CONTENT, path)
                .code("ERR_INVALID_PASSWORD_001")
                .message(ex.getMessage())
                .developerMessage("La contraseña actual no coincide con la registrada en la base de datos.")
                .build();
    }


    // ==========================================
    //     ERROR: 500 (INTERNAL SERVER ERROR)
    // ==========================================

    public StandarErrorResponse toSystemErrorResponse(Exception ex, String path) {
        return buildBaseError(HttpStatus.INTERNAL_SERVER_ERROR, path)
                .code("SYS_500")
                .message("Ocurrió un error inesperado en el servidor. Por favor, intenta más tarde.")
                .developerMessage(ex.toString())
                .build();
    }

}
