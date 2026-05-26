package com.iamfit.ejercicios.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectRoutineRequest {

    @NotNull
    private String sessionId; // ID de la sesión de generación

    @NotNull
    @Min(0) @Max(2)
    private Integer selectedIndex; // 0, 1 o 2

    private String customName; // nombre opcional para la rutina
}