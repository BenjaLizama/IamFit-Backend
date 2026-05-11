package com.iamfit.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DTO that captures all user preferences needed to generate a personalized meal plan.
 */
@Data
public class UserPreferencesRequest {

    /**
     * The user's main goal.
     * Allowed values: "Ganar músculo", "Bajar de peso", "Mantener peso"
     */
    @NotBlank(message = "El objetivo no puede estar vacío")
    private String goal;

    /**
     * Dietary preferences / eating style.
     * Examples: "Vegano", "Keto", "sin gluten", "Mediterráneo"
     */
    private List<String> preferences;

    /**
     * Foods the user is allergic to — MUST be excluded from every meal.
     * Examples: ["mariscos", "nueces", "lácteos"]
     */
    private List<String> allergies;

    /**
     * Foods the user enjoys and would like to see included when possible.
     */
    private List<String> likes;

    /**
     * Foods the user dislikes and must never appear in the plan.
     */
    private List<String> dislikes;
}
