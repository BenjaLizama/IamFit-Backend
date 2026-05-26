package com.iamfit.ejercicios.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GeneratedRoutinesResponse {
    private String sessionId;
    private List<RoutineDto> routines;
    private String message;
}