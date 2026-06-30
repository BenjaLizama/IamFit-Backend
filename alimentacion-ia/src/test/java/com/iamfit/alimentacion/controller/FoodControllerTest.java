package com.iamfit.alimentacion.controller;

import com.iamfit.alimentacion.dto.*;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import com.iamfit.alimentacion.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodControllerTest {

    @Mock private FoodCatalogService foodCatalogService;
    @Mock private FoodLogService foodLogService;
    @Mock private NutritionService nutritionService;
    @Mock private MealPlannerService mealPlannerService;
    @Mock private MealPlanService mealPlanService;

    @InjectMocks
    private FoodController controller;

    private Jwt jwt;
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);
        lenient().when(jwt.getClaim("userId")).thenReturn(userId);
    }

    @Test
    void searchFood_returnsOk() {
        FoodSearchResponse resp = FoodSearchResponse.builder().build();
        when(foodCatalogService.search("pollo")).thenReturn(resp);

        ResponseEntity<FoodSearchResponse> result = controller.searchFood("pollo");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(resp);
    }

    @Test
    void getByCategory_returnsOk() {
        when(foodCatalogService.getByCategory("carnes")).thenReturn(List.of());

        assertThat(controller.getByCategory("carnes").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getFoodById_returnsOk() {
        UUID id = UUID.randomUUID();
        FoodItemDto dto = FoodItemDto.builder().id(id).build();
        when(foodCatalogService.getById(id)).thenReturn(dto);

        assertThat(controller.getFoodById(id).getBody()).isEqualTo(dto);
    }

    @Test
    void getFoodById_propagatesException() {
        UUID id = UUID.randomUUID();
        when(foodCatalogService.getById(id)).thenThrow(new RuntimeException("not found"));

        assertThatThrownBy(() -> controller.getFoodById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void addFood_returnsOk() {
        AddFoodRequest req = new AddFoodRequest();
        FoodEntryDto dto = FoodEntryDto.builder().mealType(MealType.CENA).build();
        when(foodLogService.addFood(userId, req)).thenReturn(dto);

        assertThat(controller.addFood(req, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void deleteFood_returnsOk() {
        UUID entryId = UUID.randomUUID();
        DeleteFoodEntryResponse resp = DeleteFoodEntryResponse.builder().deletedId(entryId).build();
        when(foodLogService.deleteFood(userId, entryId)).thenReturn(resp);

        assertThat(controller.deleteFood(entryId, jwt).getBody()).isEqualTo(resp);
    }

    @Test
    void getEntriesForDay_returnsOk() {
        when(foodLogService.getEntriesForDay(userId, null)).thenReturn(List.of());

        assertThat(controller.getEntriesForDay(null, jwt).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getNutritionSummary_returnsOk() {
        NutritionSummaryDto dto = NutritionSummaryDto.builder().userId(userId).build();
        when(nutritionService.getDailySummary(userId, null)).thenReturn(dto);

        assertThat(controller.getNutritionSummary(null, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void generateMealPlan_returnsOk() {
        UserPreferencesRequest req = new UserPreferencesRequest();
        MealPlanResponse resp = new MealPlanResponse();
        when(mealPlannerService.generateMealPlan(req)).thenReturn(resp);

        assertThat(controller.generateMealPlan(req).getBody()).isEqualTo(resp);
    }

    @Test
    void saveMealPlan_returnsOk() {
        SaveMealPlanRequest req = new SaveMealPlanRequest();
        MealPlanDto dto = MealPlanDto.builder().build();
        when(mealPlanService.saveMealPlan(userId, req)).thenReturn(dto);

        assertThat(controller.saveMealPlan(req, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void getMealPlans_returnsOk() {
        when(mealPlanService.getMealPlans(userId, "ALL")).thenReturn(List.of());

        assertThat(controller.getMealPlans("ALL", jwt).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getActiveMealPlan_returnsOk() {
        MealPlanDto dto = MealPlanDto.builder().build();
        when(mealPlanService.getActiveMealPlan(userId)).thenReturn(dto);

        assertThat(controller.getActiveMealPlan(jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void activateMealPlan_returnsOk() {
        UUID planId = UUID.randomUUID();
        MealPlanDto dto = MealPlanDto.builder().build();
        when(mealPlanService.activateMealPlan(userId, planId)).thenReturn(dto);

        assertThat(controller.activateMealPlan(planId, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void deactivateMealPlan_returnsOk() {
        UUID planId = UUID.randomUUID();
        MealPlanDto dto = MealPlanDto.builder().build();
        when(mealPlanService.deactivateMealPlan(userId, planId)).thenReturn(dto);

        assertThat(controller.deactivateMealPlan(planId, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void deleteMealPlan_returnsNoContent() {
        UUID planId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.deleteMealPlan(planId, jwt);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mealPlanService).deleteMealPlan(userId, planId);
    }

    @Test
    void getMealPlanLimits_returnsOk() {
        MealPlanLimitsDto dto = MealPlanLimitsDto.builder().build();
        when(mealPlanService.getLimits(userId)).thenReturn(dto);

        assertThat(controller.getMealPlanLimits(jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void editFood_returnsOk() {
        UUID entryId = UUID.randomUUID();
        EditFoodEntryRequest req = new EditFoodEntryRequest();
        FoodEntryDto dto = FoodEntryDto.builder().build();
        when(foodLogService.editFood(userId, entryId, req)).thenReturn(dto);

        assertThat(controller.editFood(entryId, req, jwt).getBody()).isEqualTo(dto);
    }

    @Test
    void getFoodLimits_returnsOk() {
        FoodLimitsDto dto = FoodLimitsDto.builder().build();
        when(nutritionService.getFoodLimits(userId, LocalDate.now())).thenReturn(dto);

        assertThat(controller.getFoodLimits(LocalDate.now(), jwt).getBody()).isEqualTo(dto);
    }
}
