package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.WorkoutSessionExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutSessionExerciseRepository extends JpaRepository<WorkoutSessionExercise, UUID> {
    Optional<WorkoutSessionExercise> findBySessionIdAndRoutineExerciseId(
            UUID sessionId, UUID routineExerciseId);
    List<WorkoutSessionExercise> findBySessionId(UUID sessionId);
}