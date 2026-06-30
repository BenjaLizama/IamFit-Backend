package com.iamfit.alimentacion.dto;

import java.time.LocalDate;

public record ConsumeMealRequest(LocalDate date, Boolean createFoodLogEntries) {}