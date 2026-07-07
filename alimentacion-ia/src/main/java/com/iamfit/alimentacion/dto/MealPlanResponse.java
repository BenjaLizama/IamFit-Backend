package com.iamfit.alimentacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MealPlanResponse {

    @JsonProperty("objetivo")
    private String objetivo;

    @JsonProperty("menu")
    private WeekMenu menu;

    @JsonProperty("recomendaciones_nutricionales")
    private String recomendacionesNutricionales;

    @Data
    public static class WeekMenu {
        private DayMenu lunes;
        private DayMenu martes;
        private DayMenu miercoles;
        private DayMenu jueves;
        private DayMenu viernes;
        private DayMenu sabado;
        private DayMenu domingo;
    }

    @Data
    public static class DayMenu {
        // En vez de String, ahora usamos la nueva clase MealInfo
        private MealInfo desayuno;
        private MealInfo almuerzo;
        private MealInfo cena;
        private List<MealInfo> snacks;
    }

    @Data
    public static class MealInfo {
        @JsonProperty("descripcion")
        private String descripcion;

        @JsonProperty("calorias")
        private Double calorias;

        @JsonProperty("proteina")
        private Double proteina;

        @JsonProperty("carbohidratos")
        private Double carbohidratos;

        @JsonProperty("grasa")
        private Double grasa;
    }
}