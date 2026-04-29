package com.iamfit.vertex_ai_service.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIResponseDTO {
    private String response;
    private String model;
    private String status;
}