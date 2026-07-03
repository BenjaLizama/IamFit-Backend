package com.iamfit.alimentacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "meal_completions", uniqueConstraints = @UniqueConstraint(
        columnNames = {"meal_plan_id", "log_date", "meal_type"}))
public class MealCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "meal_plan_id", nullable = false)
    private UUID mealPlanId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "day_key", nullable = false)
    private String dayKey; // lunes, martes, miercoles...

    @Column(name = "meal_type", nullable = false)
    private String mealType; // desayuno, almuerzo, cena, snacks

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;
}