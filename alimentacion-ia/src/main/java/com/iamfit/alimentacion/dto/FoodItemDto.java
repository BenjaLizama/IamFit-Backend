package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class FoodItemDto {
    private UUID id;
    private String name;
    private String foodCategory;
    private Double servingSizeG;

    // valores por 100g
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
}