package com.iamfit.ai_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WellbeingResponseDTO {
    private String response;
    private String tipo;      // "CHECK_IN", "MOTIVATION", "TECHNIQUE", "SUMMARY"
    private String status;
    private String disclaimer;
}
