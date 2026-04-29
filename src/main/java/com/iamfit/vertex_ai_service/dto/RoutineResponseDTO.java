package com.iamfit.vertex_ai_service.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineResponseDTO {
    private String userId;
    private String rutina;
    private String objetivo;
    private String nivel;
    private int diasDisponibles;
    private String status;
}