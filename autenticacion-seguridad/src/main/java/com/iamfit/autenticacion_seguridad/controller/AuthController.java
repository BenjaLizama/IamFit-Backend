package com.iamfit.autenticacion_seguridad.controller;

import com.iamfit.autenticacion_seguridad.dto.AuthResponse;
import com.iamfit.autenticacion_seguridad.dto.LoginWrapper;
import com.iamfit.autenticacion_seguridad.dto.RegisterWrapper;
import com.iamfit.autenticacion_seguridad.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterWrapper registerWrapper) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerWrapper));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginWrapper loginWrapper) {
        return ResponseEntity.ok(authService.login(loginWrapper));
    }

}
