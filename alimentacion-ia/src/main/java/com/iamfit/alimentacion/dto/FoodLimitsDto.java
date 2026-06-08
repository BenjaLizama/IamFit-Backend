package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FoodLimitsDto {
    private Integer maxFoodEntriesPerDay;
    private Long entriesForDate;
    private Boolean canAddFood;
}