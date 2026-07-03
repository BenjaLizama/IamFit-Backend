package com.iamfit.ejercicios.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "workout_session_exercises", uniqueConstraints = @UniqueConstraint(
        columnNames = {"session_id", "routine_exercise_id"}))
public class WorkoutSessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false)
    private RoutineExercise routineExercise;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "sets_completed")
    private Integer setsCompleted;

    @Column(name = "reps_completed")
    private Integer repsCompleted;

    @Column(name = "weight_used")
    private Double weightUsed;

    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}