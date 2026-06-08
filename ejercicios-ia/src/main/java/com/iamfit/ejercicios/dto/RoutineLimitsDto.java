package com.iamfit.ejercicios.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineLimitsDto {
    private Integer maxActiveRoutines;
    private Long activeRoutines;
    private Long inactiveRoutines;
    private Boolean canCreateRoutine;
}