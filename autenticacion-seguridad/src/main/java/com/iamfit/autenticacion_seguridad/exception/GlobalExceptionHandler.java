package com.iamfit.autenticacion_seguridad.exception;

import com.iamfit.autenticacion_seguridad.dto.StandarErrorResponse;
import com.iamfit.autenticacion_seguridad.mapper.ErrorMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMapper errorMapper;

    /**
     * 1. Manejo de errores de validación DTO (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandarErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Error de validación en la ruta {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMapper.toValidationResponse(ex, request.getRequestURI()));
    }

    /**
     * 2. Errores de reglas de negocio (ej: El correo ya existe).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandarErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Error de negocio en la ruta {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMapper.toBusinessResponse(ex, request.getRequestURI()));
    }

    /**
     * 3. Errores de credenciales (correo o contraseña incorrectos).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandarErrorResponse> handleBadCredentialException(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Intento de login fallido en la ruta {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorMapper.toAuthResponse(ex, request.getRequestURI()));
    }

    /**
     * 4. JSON Malformado enviado por el cliente.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<StandarErrorResponse> handleHttpMessageNotReadable(Exception ex, HttpServletRequest request) {
        log.warn("Petición malformada en la ruta {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMapper.toMalformedRequestResponse(ex, request.getRequestURI()));
    }

    /**
     * 5. Acceso Denegado (403 Forbidden) - El usuario no tiene el rol necesario.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<StandarErrorResponse> handleAccessDeniedException(Exception ex, HttpServletRequest request) {
        log.warn("Acceso denegado en la ruta {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorMapper.toAccessDeniedResponse(ex, request.getRequestURI()));
    }

    /**
     * 6. Método HTTP no soportado (ej. usar GET en lugar de POST).
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandarErrorResponse> handleMethodNotSupportedException(Exception ex, HttpServletRequest request) {
        log.warn("Método HTTP no soportado en la ruta {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorMapper.toMethodNotSupportedResponse(ex, request.getRequestURI()));
    }

    /**
     * 7. Manejo de error por correo existente.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<StandarErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Intento de registro con correo duplicado en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMapper.toBusinessResponse(new IllegalArgumentException(ex.getMessage()), request.getRequestURI()));
    }

    /**
     * 8. Error de configuración interna (Roles no encontrados).
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<StandarErrorResponse> handleRoleNotFoundException(RoleNotFoundException ex, HttpServletRequest request) {
        log.error("ERROR CRÍTICO: Se intentó asignar un rol que no existe en la BD: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMapper.toSystemErrorResponse(ex, request.getRequestURI()));
    }

    /**
     * 9. Error, el RefreshToken es inválido o ya expiró.
     */
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<StandarErrorResponse> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex, HttpServletRequest request) {
        log.warn("Intento de acceso con token inválido o expirado en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMapper.toRefreshTokenNotFound(ex, request.getRequestURI()));
    }

    /**
     * 10. Error de credenciales no encontradas
     */
    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<StandarErrorResponse> handleCredentialNotFound(CredentialNotFoundException ex, HttpServletRequest request) {
        log.warn("Credencial no encontrada o intento de acceso invalido en {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // No entregamos información de si existe el usuario.
                .body(errorMapper.toBusinessResponse(new IllegalArgumentException(ex.getMessage()), request.getRequestURI()));
    }

    /**
     * 11. Error las llaves no se leyeron/generaron
     */
    @ExceptionHandler(RsaKeyInitializationException.class)
    public ResponseEntity<StandarErrorResponse> handleRsaKeyInitialization(RsaKeyInitializationException ex, HttpServletRequest request) {
        log.error("Error crítico de inicialización RSA: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMapper.toSystemErrorResponse(ex, request.getRequestURI()));
    }

    /**
     * 12. Error de cuenta desactivada.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<StandarErrorResponse> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        log.error("Intento de acceso a cuenta deshabilitada en {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorMapper.toAccountDisabledResponse(new IllegalArgumentException(ex.getMessage()), request.getRequestURI()));
    }

    /**
     * 13. Intento de uso de refresh token desde un dispositivo distinto al de origen.
     */
    // antes generico 500, ahora 403
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<StandarErrorResponse> handleSecurityException(SecurityException ex, HttpServletRequest request) {
        log.warn("Violación de seguridad de sesión en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorMapper.toSecurityViolationResponse(ex, request.getRequestURI()));
    }

    /**
     * 14. Proveedor de autenticación no implementado o no soportado.
     */
    @ExceptionHandler(UnsupportedAuthenticationProviderException.class)
    public ResponseEntity<StandarErrorResponse> handleUnsupportedProvider(UnsupportedAuthenticationProviderException ex, HttpServletRequest request) {
        log.warn("Estrategia de autenticación no disponible en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorMapper.toStrategyNotImplementedResponse(ex, request.getRequestURI()));
    }

    /**
     * 15. 422 contraseña inválida
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<StandarErrorResponse> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        log.warn("Contraseña incorrecta en {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorMapper.toInvalidPasswordResponse(ex, request.getRequestURI()));
    }

    /**
     * 16. Conflicto de concurrencia optimista.
     */
    @ExceptionHandler({
            jakarta.persistence.OptimisticLockException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<StandarErrorResponse> handleOptimisticLock(Exception ex, HttpServletRequest request) {
        log.warn("Conflicto de concurrencia en {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorMapper.toConcurrencyConflictResponse(ex, request.getRequestURI()));
    }

    /**
     * 0. Error genérico de servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Error crítico inesperado en la ruta {}: ", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMapper.toSystemErrorResponse(ex, request.getRequestURI()));
    }

}
