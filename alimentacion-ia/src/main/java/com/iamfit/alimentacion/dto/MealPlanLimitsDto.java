package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MealPlanLimitsDto {
    private Integer maxSavedMealPlans;
    private Integer maxActiveMealPlans;
    private Long savedMealPlans;
    private Long activeMealPlans;
    private Boolean canSaveMealPlan;
    private Boolean canActivateMealPlan;
}