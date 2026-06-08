package com.iamfit.alimentacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "meal_plans")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    private String goal;

    @Enumerated(EnumType.STRING)
    private MealPlanStatus status;

    @Column(name = "menu_json", columnDefinition = "TEXT")
    private String menuJson; // MealPlanResponse serializado como JSON

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Enumerated(EnumType.STRING)
    private MealPlanSource source;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = MealPlanStatus.INACTIVE;
        if (source == null) source = MealPlanSource.AI;
    }

    public enum MealPlanStatus { ACTIVE, INACTIVE }
    public enum MealPlanSource { AI, MANUAL }
}