package com.iamfit.ai_service.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class RoutineRequestDTO {
    @NotBlank(message = "El objetivo no puede estar vacío")
    private String objetivo;        // "hipertrofia", "fuerza", "resistencia", "perder peso"

    @Min(value = 1, message = "Mínimo 1 día")
    @Max(value = 7, message = "Máximo 7 días")
    private int diasDisponibles;

    @NotBlank(message = "El nivel no puede estar vacío")
    private String nivel;           // "principiante", "intermedio", "avanzado"

    private List<String> lesiones;
}