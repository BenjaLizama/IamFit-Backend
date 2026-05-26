package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.WorkoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutHistoryRepository extends JpaRepository<WorkoutHistory, UUID> {

    Optional<WorkoutHistory> findByUserIdAndWorkoutDate(String userId, LocalDate date);

    List<WorkoutHistory> findByUserIdOrderByWorkoutDateDesc(String userId);

    List<WorkoutHistory> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
            String userId, LocalDate from, LocalDate to);

    boolean existsByUserIdAndWorkoutDate(String userId, LocalDate date);
}