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
        private String desayuno;
        private String almuerzo;
        private String cena;
        private List<String> snacks;
    }
}
