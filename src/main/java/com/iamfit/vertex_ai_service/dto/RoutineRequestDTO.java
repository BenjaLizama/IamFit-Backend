package com.iamfit.vertex_ai_service.dto;


import lombok.Data;
import java.util.List;

@Data
public class RoutineRequestDTO {
    private String userId;
    private String objetivo;        // "hipertrofia", "fuerza", "resistencia", "perder peso"
    private int diasDisponibles;    // 3, 4, 5...
    private String nivel;           // "principiante", "intermedio", "avanzado"
    private List<String> lesiones;  // ej: ["rodilla", "hombro"] o vacío
}