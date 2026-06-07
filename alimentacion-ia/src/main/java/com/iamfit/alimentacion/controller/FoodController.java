package com.iamfit.alimentacion.controller;

import com.iamfit.alimentacion.dto.*;
import com.iamfit.alimentacion.service.FoodCatalogService;
import com.iamfit.alimentacion.service.FoodLogService;
import com.iamfit.alimentacion.service.NutritionService;
import com.iamfit.alimentacion.service.MealPlannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodCatalogService foodCatalogService;
    private final FoodLogService foodLogService;
    private final NutritionService nutritionService;
    private final MealPlannerService mealPlannerService;

    // ─── Catálogo ────────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<FoodSearchResponse> searchFood(
            @RequestParam String query) {
        return ResponseEntity.ok(foodCatalogService.search(query));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FoodItemDto>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(foodCatalogService.getByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItemDto> getFoodById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(foodCatalogService.getById(id));
    }

    // ─── Registro diario ─────────────────────────────────────────────

    @PostMapping("/addFood")
    public ResponseEntity<FoodEntryDto> addFood(
            @Valid @RequestBody AddFoodRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(foodLogService.addFood(userId, request));
    }

    @DeleteMapping("/addFood/{entryId}")
    public ResponseEntity<DeleteFoodEntryResponse> deleteFood(
            @PathVariable UUID entryId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(foodLogService.deleteFood(userId, entryId));
    }

    @GetMapping("/addFood")
    public ResponseEntity<List<FoodEntryDto>> getEntriesForDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(foodLogService.getEntriesForDay(userId, date));
    }

    // ─── Valores nutricionales ───────────────────────────────────────

    @GetMapping("/calories")
    public ResponseEntity<NutritionSummaryDto> getNutritionSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getDailySummary(userId, date));
    }

    // ─── Planificador de Comidas ─────────────────────────────────────

    /**
     * POST /api/v1/food/meal-plan/generate
     *
     * Generates a personalised weekly meal plan.
     *
     * Example body:
     * {
     * "goal": "Ganar músculo",
     * "preferences": ["Alta proteína"],
     * "allergies": ["mariscos"],
     * "likes": ["pollo", "arroz", "huevos"],
     * "dislikes": ["brócoli"]
     * }
     */
    @PostMapping("/meal-plan/generate")
    public ResponseEntity<MealPlanResponse> generateMealPlan(
            @Valid @RequestBody UserPreferencesRequest request) {

        log.info("Received meal plan request — goal: {}", request.getGoal());
        MealPlanResponse response = mealPlannerService.generateMealPlan(request);
        return ResponseEntity.ok(response);
    }
}