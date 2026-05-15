package com.iamfit.ai_service.controller;

import com.iamfit.ai_service.dto.*;
import com.iamfit.ai_service.service.WellbeingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wellbeing")
@RequiredArgsConstructor
public class WellbeingController {

    private final WellbeingService wellbeingService;

    @PostMapping("/check-in")
    public ResponseEntity<WellbeingResponseDTO> checkIn(
            @RequestBody @Valid WellbeingCheckInRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(wellbeingService.checkIn(userId, request));
    }

    @PostMapping("/motivation")
    public ResponseEntity<WellbeingResponseDTO> getMotivation(
            @RequestBody MotivationRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(wellbeingService.getMotivation(userId, request));
    }

    @PostMapping("/techniques")
    public ResponseEntity<WellbeingResponseDTO> getTechnique(
            @RequestBody TechniqueRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(wellbeingService.getTechnique(userId, request));
    }
}
