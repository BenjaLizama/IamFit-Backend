package com.iamfit.alimentacion.controller;

import com.iamfit.alimentacion.dto.*;
import com.iamfit.alimentacion.service.*;
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
import java.util.Map;
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
    private final MealPlanService mealPlanService;
    private final MealCompletionService mealCompletionService;


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

    // ─── Gestión de planes de comida ─────────────────────────────────

    @PostMapping("/meal-plans")
    public ResponseEntity<MealPlanDto> saveMealPlan(
            @Valid @RequestBody SaveMealPlanRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.saveMealPlan(userId, request));
    }

    @GetMapping("/meal-plans")
    public ResponseEntity<List<MealPlanDto>> getMealPlans(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.getMealPlans(userId, status));
    }

    @GetMapping("/meal-plans/active")
    public ResponseEntity<MealPlanDto> getActiveMealPlan(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.getActiveMealPlan(userId));
    }

    @PatchMapping("/meal-plans/{planId}/activate")
    public ResponseEntity<MealPlanDto> activateMealPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.activateMealPlan(userId, planId));
    }

    @PatchMapping("/meal-plans/{planId}/deactivate")
    public ResponseEntity<MealPlanDto> deactivateMealPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.deactivateMealPlan(userId, planId));
    }

    @DeleteMapping("/meal-plans/{planId}")
    public ResponseEntity<Void> deleteMealPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        mealPlanService.deleteMealPlan(userId, planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meal-plans/limits")
    public ResponseEntity<MealPlanLimitsDto> getMealPlanLimits(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealPlanService.getLimits(userId));
    }

    @PatchMapping("/addFood/{entryId}")
    public ResponseEntity<FoodEntryDto> editFood(
            @PathVariable UUID entryId,
            @RequestBody EditFoodEntryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(foodLogService.editFood(userId, entryId, request));
    }

    @GetMapping("/limits")
    public ResponseEntity<FoodLimitsDto> getFoodLimits(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getFoodLimits(userId, date));
    }

    @GetMapping("/calories/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyCalories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getMonthlyCaloriesSummary(userId));
    }

    @GetMapping("/protein/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyProtein(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getMonthlyProteinSummary(userId));
    }

    @GetMapping("/calories/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyCalories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getDailyCaloriesSummary(userId));
    }

    @GetMapping("/calories/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyCalories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getWeeklyCaloriesSummary(userId));
    }

    @GetMapping("/protein/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyProtein(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getDailyProteinSummary(userId));
    }

    @GetMapping("/protein/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyProtein(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(nutritionService.getWeeklyProteinSummary(userId));
    }

    // ─── Progreso y completado de plan de comidas ───────────────────────

    @GetMapping("/meal-plans/active/progress")
    public ResponseEntity<MealPlanProgressResponse> getActivePlanProgress(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealCompletionService.getActivePlanProgress(userId));
    }

    @PatchMapping("/meal-plans/{planId}/days/{day}/meals/{mealId}/consume")
    public ResponseEntity<MealCompletionResponse> consumeMeal(
            @PathVariable UUID planId,
            @PathVariable String day,
            @PathVariable String mealId,
            @RequestBody(required = false) ConsumeMealRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        ConsumeMealRequest safeRequest = request != null ? request : new ConsumeMealRequest(null, false);
        return ResponseEntity.ok(mealCompletionService.consumeMeal(userId, planId, day, mealId, safeRequest));
    }

    @PatchMapping("/meal-plans/{planId}/days/{day}/meals/{mealId}/unconsume")
    public ResponseEntity<MealCompletionResponse> unconsumeMeal(
            @PathVariable UUID planId,
            @PathVariable String day,
            @PathVariable String mealId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealCompletionService.unconsumeMeal(userId, planId, day, mealId));
    }

    @PatchMapping("/meal-plans/{planId}/days/{day}/complete")
    public ResponseEntity<MealPlanDayCompleteResponse> completeDay(
            @PathVariable UUID planId,
            @PathVariable String day,
            @RequestBody(required = false) CompleteMealPlanDayRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        CompleteMealPlanDayRequest safeRequest = request != null
                ? request : new CompleteMealPlanDayRequest(null, true, false);
        return ResponseEntity.ok(mealCompletionService.completeDay(userId, planId, day, safeRequest));
    }

    @GetMapping("/meal-plans/history")
    public ResponseEntity<MealPlanHistoryResponse> getMealPlanHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(mealCompletionService.getHistory(userId, from, to));
    }
}