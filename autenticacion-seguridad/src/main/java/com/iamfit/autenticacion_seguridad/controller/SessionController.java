package com.iamfit.autenticacion_seguridad.controller;

import com.iamfit.autenticacion_seguridad.dto.AuthResponse;
import com.iamfit.autenticacion_seguridad.dto.ChangePasswordRequest;
import com.iamfit.autenticacion_seguridad.dto.DeactivateResponse;
import com.iamfit.autenticacion_seguridad.dto.RefreshTokenWrapper;
import com.iamfit.autenticacion_seguridad.service.impl.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenWrapper refreshTokenWrapper) {
        return ResponseEntity.ok(sessionService.refreshToken(refreshTokenWrapper));
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<DeactivateResponse> deactivate() {
        return ResponseEntity.ok(sessionService.deactivateSelf());
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(sessionService.changePassword(request));
    }

    // extrae el token de header autorization y se lo pasa al service
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        sessionService.logout();
        return ResponseEntity.noContent().build();
    }

}
