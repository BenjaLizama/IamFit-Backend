package com.iamfit.vertex_ai_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackResponseDTO {
    private String userId;
    private String feedback;
    private String status;
}