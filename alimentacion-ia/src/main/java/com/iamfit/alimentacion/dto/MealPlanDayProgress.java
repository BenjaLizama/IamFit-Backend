package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class MealPlanDayProgress {
    private String day;
    private Boolean completed;
    private Instant completedAt;
    private List<MealProgressItem> meals;
}