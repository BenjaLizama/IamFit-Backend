package com.iamfit.vertex_ai_service.dto;


import lombok.Data;
import java.util.List;

@Data
public class FeedbackRequestDTO {

    private String userId;

    // Estadísticas diarias
    private double pesoActual;          // kg
    private int caloriasConsumidas;     // kcal
    private double aguaConsumida;       // litros
    private double horasSueno;          // horas
    private List<String> ejerciciosRealizados; // ej: ["Press banca", "Sentadilla"]

    // Estadísticas semanales
    private double pesoInicialSemana;   // kg
    private int promedioCaloriasSemana; // kcal promedio diario
    private double promedioAguaSemana;  // litros promedio diario
    private double promedioSuenoSemana; // horas promedio diario
    private int diasEntrenadosSemana;   // días que entrenó
    private String objetivo;            // "bajar", "subir", "mantener"
}