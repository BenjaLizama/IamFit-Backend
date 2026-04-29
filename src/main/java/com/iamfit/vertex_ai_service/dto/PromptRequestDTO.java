package com.iamfit.vertex_ai_service.dto;

import lombok.Data;

@Data
public class PromptRequestDTO {
    private String userId;
    private String message;
}