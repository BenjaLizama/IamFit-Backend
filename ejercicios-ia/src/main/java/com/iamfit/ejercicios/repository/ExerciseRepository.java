package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.Exercise;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByIsActiveTrue();

    List<Exercise> findByMuscleGroupAndIsActiveTrue(MuscleGroup muscleGroup);

    List<Exercise> findByDifficultyAndIsActiveTrue(DifficultyLevel difficulty);

    List<Exercise> findByMuscleGroupAndDifficultyAndIsActiveTrue(
            MuscleGroup muscleGroup, DifficultyLevel difficulty);

    @Query("SELECT e FROM Exercise e WHERE e.isActive = true AND " +
            "(:muscleGroup IS NULL OR e.muscleGroup = :muscleGroup) AND " +
            "(:difficulty IS NULL OR e.difficulty = :difficulty) AND " +
            "(:equipment IS NULL OR e.equipment = :equipment)")
    List<Exercise> findByFilters(
            @Param("muscleGroup") MuscleGroup muscleGroup,
            @Param("difficulty") DifficultyLevel difficulty,
            @Param("equipment") Equipment equipment);

    List<Exercise> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}