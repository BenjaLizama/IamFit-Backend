package com.iamfit.alimentacion.dto;

import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class NutritionSummaryDto {

    private LocalDate date;
    private String userId;

    // Totales del día
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbohydrates;
    private Double totalFat;
    private Double totalFiber;

    // Desglose por tipo de comida
    private Map<MealType, List<FoodEntryDto>> entriesByMeal;

    // Totales por tipo de comida
    private Map<MealType, MealNutritionDto> mealTotals;

    @Data
    @Builder
    public static class MealNutritionDto {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Double fiber;
    }
}