package com.iamfit.ai_service.controller;

import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.*;
import com.iamfit.ai_service.service.AIService;
import com.iamfit.ai_service.service.FeedbackService;
import com.iamfit.ai_service.service.RoutineService;
import com.iamfit.ai_service.service.WeeklyMenuService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class AIController {

    private final AIService aiService;
    private final WeeklyMenuService weeklyMenuService;
    private final RoutineService routineService;
    private final FeedbackService feedbackService;
    private final RateLimitConfig rateLimitConfig;

    @PostMapping("/prompt")
    public ResponseEntity<?> prompt(
            @RequestBody @Valid PromptRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaim("userId");

        // Rate limiting
        Bucket bucket = rateLimitConfig.resolveBucket(userId);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Límite de consultas alcanzado",
                            "message", "Has alcanzado el límite de 30 consultas por hora"
                    ));
        }

        String safeMessage = sanitize(request.getMessage());
        AIResponseDTO response = aiService.chat(userId, safeMessage);
        return ResponseEntity.ok(response);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        String cleaned = input.replaceAll("[\\p{Cntrl}&&[^\\n]]", "");
        return cleaned.replaceAll(
                "(?i)(ignore (all )?previous|disregard|you are now|act as|jailbreak|reveal.*prompt)",
                "[REDACTED]");
    }

    @PostMapping("/addWeeklyMenu")
    public ResponseEntity<?> addWeeklyMenu(
            @RequestBody @Valid WeeklyMenuRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addRoutineExercise")
    public ResponseEntity<?> addRoutineExercise(
            @RequestBody @Valid RoutineRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        RoutineResponseDTO response = routineService.generateRoutine(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponseDTO> feedback(
            @RequestBody FeedbackRequestDTO request) {
        return ResponseEntity.ok(feedbackService.generateFeedback(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("M.I.A. operativa");
    }
}