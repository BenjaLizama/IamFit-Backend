package com.iamfit.ai_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeeklyMenuResponseDTO {
    private String userId;
    private String menu;
    private String objetivo;
    private int calorias;
    private String status;
    private String disclaimer;
}