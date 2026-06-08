package com.iamfit.alimentacion.dto;

import com.iamfit.alimentacion.entity.MealPlan.MealPlanSource;
import com.iamfit.alimentacion.entity.MealPlan.MealPlanStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MealPlanDto {
    private UUID id;
    private String title;
    private String goal;
    private MealPlanStatus status;
    private MealPlanSource source;
    private MealPlanResponse menu;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
}