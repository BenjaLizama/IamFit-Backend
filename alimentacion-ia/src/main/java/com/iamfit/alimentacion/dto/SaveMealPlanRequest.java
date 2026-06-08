package com.iamfit.alimentacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveMealPlanRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    private String goal;
    private MealPlanResponse.WeekMenu menu;
    private String recomendacionesNutricionales;
}