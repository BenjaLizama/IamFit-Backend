package com.iamfit.alimentacion.dto;

import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EditFoodEntryRequest {
    private Double quantity;
    private MealType mealType;
    private LocalDate logDate;
}