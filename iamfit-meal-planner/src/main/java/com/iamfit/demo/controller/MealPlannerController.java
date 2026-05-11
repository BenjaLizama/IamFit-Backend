package com.iamfit.demo.controller;

import com.iamfit.demo.dto.MealPlanResponse;
import com.iamfit.demo.dto.UserPreferencesRequest;
import com.iamfit.demo.service.MealPlannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/meal-plan")
@RequiredArgsConstructor
public class MealPlannerController {

    private final MealPlannerService mealPlannerService;

    /**
     * POST /api/v1/meal-plan/generate
     *
     * Generates a personalised weekly meal plan.
     *
     * Example body:
     * {
     *   "goal": "Ganar músculo",
     *   "preferences": ["Alta proteína"],
     *   "allergies": ["mariscos"],
     *   "likes": ["pollo", "arroz", "huevos"],
     *   "dislikes": ["brócoli"]
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<MealPlanResponse> generateMealPlan(
            @Valid @RequestBody UserPreferencesRequest request) {

        log.info("Received meal plan request — goal: {}", request.getGoal());
        MealPlanResponse response = mealPlannerService.generateMealPlan(request);
        return ResponseEntity.ok(response);
    }
}
