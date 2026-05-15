package com.iamfit.ai_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class WeeklyMenuRequestDTO {
    @NotBlank(message = "El objetivo no puede estar vacío")
    private String objetivo;        // "bajar", "subir", "mantener"

    @Min(value = 1000, message = "Mínimo 1000 kcal")
    @Max(value = 5000, message = "Máximo 5000 kcal")
    private int calorias;

    private List<String> alergias;
}