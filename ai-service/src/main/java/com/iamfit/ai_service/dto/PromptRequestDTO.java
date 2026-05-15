package com.iamfit.ai_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptRequestDTO {
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 2000, message = "El mensaje no puede superar los 2000 caracteres")
    private String message;
}