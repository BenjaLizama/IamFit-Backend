package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    Optional<WorkoutSession> findByIdAndUserId(UUID id, String userId);
    Optional<WorkoutSession> findByRoutineIdAndUserIdAndStatus(
            UUID routineId, String userId, WorkoutSession.SessionStatus status);
}