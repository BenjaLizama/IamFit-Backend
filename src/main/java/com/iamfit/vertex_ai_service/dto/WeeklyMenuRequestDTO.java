package com.iamfit.vertex_ai_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class WeeklyMenuRequestDTO {
    private String userId;
    private String objetivo;        // "bajar", "subir", "mantener"
    private int calorias;
    private List<String> alergias; // ej: ["lactosa", "gluten"]
}