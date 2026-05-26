package com.iamfit.ejercicios.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_group", nullable = false)
    private MuscleGroup muscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Column(name = "default_sets")
    private Integer defaultSets;

    @Column(name = "default_reps")
    private Integer defaultReps;

    @Column(name = "default_rest_seconds")
    private Integer defaultRestSeconds;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // ─── Enums ───────────────────────────────────────────────────────

    public enum MuscleGroup {
        PECHO, ESPALDA, HOMBROS, BICEPS, TRICEPS,
        PIERNAS, GLUTEOS, CORE, CARDIO, CUERPO_COMPLETO
    }

    public enum Equipment {
        BARRA, MANCUERNAS, MAQUINA, POLEA,
        PESO_CORPORAL, BANDA_ELASTICA, KETTLEBELL
    }

    public enum DifficultyLevel {
        PRINCIPIANTE, INTERMEDIO, AVANZADO
    }
}