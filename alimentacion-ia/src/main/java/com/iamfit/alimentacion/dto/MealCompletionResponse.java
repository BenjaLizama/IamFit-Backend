package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MealCompletionResponse {
    private UUID planId;
    private String day;
    private String mealId;
    private Boolean completed;
    private Instant completedAt;
    private List<String> createdFoodEntryIds;
    private String warning;
}