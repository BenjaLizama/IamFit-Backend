package com.iamfit.autenticacion_seguridad.controller;

import com.iamfit.autenticacion_seguridad.dto.*;
import com.iamfit.autenticacion_seguridad.service.impl.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/request")
    public ResponseEntity<PasswordResetResponseDto> requestOtp(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        return ResponseEntity.ok(forgotPasswordService.requestOtp(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponseDto> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request) {
        return ResponseEntity.ok(forgotPasswordService.verifyOtp(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        return ResponseEntity.ok(forgotPasswordService.resetPassword(request));
    }
}