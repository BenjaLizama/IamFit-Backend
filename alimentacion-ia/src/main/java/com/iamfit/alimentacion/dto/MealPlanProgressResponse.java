package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MealPlanProgressResponse {
    private UUID planId;
    private String name;
    private String status;
    private String currentDay;
    private Integer progressPercentage;
    private List<MealPlanDayProgress> days;
}