package com.iamfit.ejercicios.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "workout_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workout_date"}))
public class WorkoutHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Enumerated(EnumType.STRING)
    private WorkoutStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private String notes;

    public enum WorkoutStatus {
        EN_PROGRESO, COMPLETADO, OMITIDO
    }
}