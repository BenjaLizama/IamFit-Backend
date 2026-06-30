package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MealPlanHistoryResponse {
    private LocalDate from;
    private LocalDate to;
    private List<MealPlanHistoryDay> days;
}