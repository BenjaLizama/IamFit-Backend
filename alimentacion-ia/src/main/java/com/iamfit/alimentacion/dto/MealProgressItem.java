package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class MealProgressItem {
    private String mealId;
    private String mealType;
    private String title;
    private Boolean completed;
    private Instant completedAt;
}