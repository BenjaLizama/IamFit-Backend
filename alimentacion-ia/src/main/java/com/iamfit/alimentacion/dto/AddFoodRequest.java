package com.iamfit.alimentacion.dto;

import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AddFoodRequest {

    @NotNull(message = "El alimento es obligatorio")
    private UUID foodItemId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double quantity; // gramos

    @NotNull(message = "El tipo de comida es obligatorio")
    private MealType mealType;

    private LocalDate logDate; // si es null se usa la fecha de hoy
}