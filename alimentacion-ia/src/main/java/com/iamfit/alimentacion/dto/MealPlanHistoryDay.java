package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MealPlanHistoryDay {
    private LocalDate date;
    private UUID planId;
    private Integer completedMeals;
    private Integer totalMeals;
    private Boolean completed;
    private Integer adherencePercentage;
}