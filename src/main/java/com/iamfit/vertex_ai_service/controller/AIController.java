package com.iamfit.vertex_ai_service.controller;

import com.iamfit.vertex_ai_service.dto.*;
import com.iamfit.vertex_ai_service.service.AIService;
import com.iamfit.vertex_ai_service.service.FeedbackService;
import com.iamfit.vertex_ai_service.service.RoutineService;
import com.iamfit.vertex_ai_service.service.WeeklyMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final WeeklyMenuService weeklyMenuService;
    private final RoutineService routineService;
    private final FeedbackService feedbackService;

    @PostMapping("/prompt")
    public ResponseEntity<AIResponseDTO> prompt(@RequestBody PromptRequestDTO request) {
        AIResponseDTO response = aiService.chat(request.getUserId(), request.getMessage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addWeeklyMenu")
    public ResponseEntity<WeeklyMenuResponseDTO> addWeeklyMenu(
            @RequestBody WeeklyMenuRequestDTO request) {
        WeeklyMenuResponseDTO response = weeklyMenuService.generateMenu(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addRoutineExercise")
    public ResponseEntity<RoutineResponseDTO> addRoutineExercise(
            @RequestBody RoutineRequestDTO request) {
        RoutineResponseDTO response = routineService.generateRoutine(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponseDTO> feedback(
            @RequestBody FeedbackRequestDTO request) {
        FeedbackResponseDTO response = feedbackService.generateFeedback(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("M.I.A. operativa");
    }
}