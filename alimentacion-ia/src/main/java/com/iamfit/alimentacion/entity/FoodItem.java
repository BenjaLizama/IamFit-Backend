package com.iamfit.alimentacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    // Valores nutricionales por 100g
    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double carbohydrates;

    @Column(nullable = false)
    private Double fat;

    private Double fiber;
    private Double sugar;
    private Double sodium;

    @Column(name = "serving_size_g")
    private Double servingSizeG; // porción estándar sugerida en gramos

    @Column(name = "food_category")
    private String foodCategory; // carnes, lacteos, cereales, frutas, verduras, etc.

    @Column(name = "external_id")
    private String externalId; // ID en USDA FDC o CalorieNinjas

    @Column(name = "is_verified")
    private Boolean isVerified = true; // false si viene de API externa sin revisión
}