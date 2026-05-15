package com.iamfit.ai_service.dto;

import lombok.Data;

@Data
public class MotivationRequestDTO {
    private String contexto;  // opcional: "antes de entrenar", "después de entrenar", etc.
}