package com.iamfit.alimentacion.dto;

import java.time.LocalDate;

public record CompleteMealPlanDayRequest(
        LocalDate date, Boolean completeAllMeals, Boolean createFoodLogEntries) {}