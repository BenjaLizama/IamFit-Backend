package com.iamfit.autenticacion_seguridad.mapper;

import com.iamfit.autenticacion_seguridad.dto.StandarErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorMapperTest {

    private ErrorMapper errorMapper;
    private final String path = "/api/test";

    @BeforeEach
    void setUp() {
        errorMapper = new ErrorMapper();
    }

    @Test
    @DisplayName("Debería mapear MethodArgumentNotValidException a error de validación 400")
    void toValidationResponseTest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "Formato inválido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        StandarErrorResponse response = errorMapper.toValidationResponse(ex, path);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("ERR_VALIDATION_001", response.code());
        assertTrue(response.validationError().containsKey("email"));
        assertEquals("Formato inválido", response.validationError().get("email"));
    }

    @Test
    @DisplayName("Debería mapear IllegalArgumentException a error de negocio 400")
    void toBusinessResponseTest() {
        IllegalArgumentException ex = new IllegalArgumentException("Usuario ya existe");
        StandarErrorResponse response = errorMapper.toBusinessResponse(ex, path);

        assertEquals("ERR_BUSINESS_001", response.code());
        assertEquals("Usuario ya existe", response.message());
        assertEquals(path, response.path());
    }

    @Test
    @DisplayName("Debería mapear Malformed Request a error 400")
    void toMalformedRequestResponseTest() {
        Exception ex = new Exception("JSON parse error");
        StandarErrorResponse response = errorMapper.toMalformedRequestResponse(ex, path);

        assertEquals("ERR_MALFORMED_001", response.code());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
    }

    @Test
    @DisplayName("Debería mapear Unsupported Provider a error 400")
    void toUnsupportedProviderResponseTest() {
        Exception ex = new Exception("Provider XYZ not found");
        StandarErrorResponse response = errorMapper.toUnsupportedProviderResponse(ex, path);

        assertEquals("AUTH_PROVIDER_001", response.code());
    }

    @Test
    @DisplayName("Debería mapear Auth Error a 401 Unauthorized")
    void toAuthResponseTest() {
        Exception ex = new Exception("Bad credentials");
        StandarErrorResponse response = errorMapper.toAuthResponse(ex, path);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertEquals("AUTH_001", response.code());
    }

    @Test
    @DisplayName("Debería mapear Refresh Token Not Found a 401 Unauthorized")
    void toRefreshTokenNotFoundTest() {
        Exception ex = new Exception("Token expired");
        StandarErrorResponse response = errorMapper.toRefreshTokenNotFound(ex, path);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertEquals("AUTH_002", response.code());
        assertEquals("Sesión inválida o expirada.", response.message());
        assertEquals("Token expired", response.developerMessage());
    }

    @Test
    @DisplayName("Debería mapear Access Denied a 403 Forbidden")
    void toAccessDeniedResponseTest() {
        Exception ex = new Exception("No roles");
        StandarErrorResponse response = errorMapper.toAccessDeniedResponse(ex, path);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.status());
        assertEquals("ERR_FORBIDDEN_001", response.code());
    }

    @Test
    @DisplayName("Debería mapear Account Disabled a 403 Forbidden")
    void toAccountDisabledResponseTest() {
        Exception ex = new Exception("Disabled");
        StandarErrorResponse response = errorMapper.toAccountDisabledResponse(ex, path);

        assertEquals("ERR_FORBIDDEN_002", response.code());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status());
    }

    @Test
    @DisplayName("Debería mapear Security Violation a 403 Forbidden")
    void toSecurityViolationResponseTest() {
        Exception ex = new Exception("Device mismatch");
        StandarErrorResponse response = errorMapper.toSecurityViolationResponse(ex, path);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.status());
        assertEquals("ERR_SECURITY_002", response.code());
        assertEquals("Esta sesión no pertenece a este dispositivo.", response.message());
    }

    @Test
    @DisplayName("Debería mapear Method Not Supported a 405")
    void toMethodNotSupportedResponseTest() {
        Exception ex = new Exception("POST not allowed");
        StandarErrorResponse response = errorMapper.toMethodNotSupportedResponse(ex, path);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.status());
        assertEquals("ERR_METHOD_001", response.code());
    }

    @Test
    @DisplayName("Debería mapear Strategy Not Implemented a 422")
    void toStrategyNotImplementedResponseTest() {
        Exception ex = new Exception("OAuth strategy missing");
        StandarErrorResponse response = errorMapper.toStrategyNotImplementedResponse(ex, path);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), response.status());
        assertEquals("AUTH_STRATEGY_001", response.code());
    }

    @Test
    @DisplayName("Debería mapear error de sistema a 500")
    void toSystemErrorResponseTest() {
        Exception ex = new RuntimeException("NullPointer");
        StandarErrorResponse response = errorMapper.toSystemErrorResponse(ex, path);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals("SYS_500", response.code());
        assertNotNull(response.timestamp());
    }
}
