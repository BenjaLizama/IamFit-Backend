package com.iamfit.alimentacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "food_entries")
public class FoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_log_id", nullable = false)
    private DailyLog dailyLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false)
    private Double quantity; // gramos consumidos

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    public enum MealType {
        DESAYUNO, ALMUERZO, CENA, SNACK
    }

    // Valores calculados al momento del registro
    // (se guardan para no depender del catálogo si cambia)
    @Column(name = "calculated_calories")
    private Double calculatedCalories;

    @Column(name = "calculated_protein")
    private Double calculatedProtein;

    @Column(name = "calculated_carbs")
    private Double calculatedCarbs;

    @Column(name = "calculated_fat")
    private Double calculatedFat;

    @Column(name = "calculated_fiber")
    private Double calculatedFiber;
}