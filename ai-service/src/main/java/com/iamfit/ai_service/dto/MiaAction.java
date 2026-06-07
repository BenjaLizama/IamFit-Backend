package com.iamfit.ai_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiaAction {
    private String type;
    private String label;
    private Object payload;
}