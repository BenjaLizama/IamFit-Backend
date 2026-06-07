package com.iamfit.ai_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIResponseDTO {
    private String content;
    private String model;
    private String status;
    private String disclaimer;
    private List<MiaAction> actions;
}