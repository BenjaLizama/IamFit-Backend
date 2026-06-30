package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MealPlanDayCompleteResponse {
    private UUID planId;
    private String day;
    private Boolean completed;
    private Instant completedAt;
    private Integer completedMeals;
    private Integer totalMeals;
}