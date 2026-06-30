package com.iamfit.alimentacion.repository;

import com.iamfit.alimentacion.entity.MealCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealCompletionRepository extends JpaRepository<MealCompletion, UUID> {

    Optional<MealCompletion> findByMealPlanIdAndLogDateAndMealType(
            UUID mealPlanId, LocalDate logDate, String mealType);

    List<MealCompletion> findByMealPlanIdAndLogDate(UUID mealPlanId, LocalDate logDate);

    List<MealCompletion> findByMealPlanIdAndUserId(UUID mealPlanId, String userId);

    List<MealCompletion> findByMealPlanIdInAndLogDateBetween(
            List<UUID> mealPlanIds, LocalDate from, LocalDate to);
}