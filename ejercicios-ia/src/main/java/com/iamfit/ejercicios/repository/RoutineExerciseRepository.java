package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, UUID> {

    List<RoutineExercise> findByRoutineIdOrderByOrderIndex(UUID routineId);

    void deleteByRoutineId(UUID routineId);
}