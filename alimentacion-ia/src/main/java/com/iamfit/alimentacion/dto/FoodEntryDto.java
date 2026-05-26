package com.iamfit.alimentacion.dto;

import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class FoodEntryDto {
    private UUID id;
    private String foodName;
    private Double quantity;
    private MealType mealType;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;
    private Double fiber;
}