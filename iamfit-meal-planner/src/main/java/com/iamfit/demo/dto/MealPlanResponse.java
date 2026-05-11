package com.iamfit.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Mirrors the JSON structure returned by the AI model.
 * Jackson deserializes the raw AI response into this object.
 */
@Data
public class MealPlanResponse {

    @JsonProperty("objetivo")
    private String objetivo;

    @JsonProperty("menu")
    private Map<String, DayMenu> menu;

    @JsonProperty("recomendaciones_nutricionales")
    private String recomendacionesNutricionales;

    // -------------------------------------------------------

    @Data
    public static class DayMenu {

        @JsonProperty("desayuno")
        private String desayuno;

        @JsonProperty("almuerzo")
        private String almuerzo;

        @JsonProperty("cena")
        private String cena;

        @JsonProperty("snacks")
        private List<String> snacks;
    }
}
