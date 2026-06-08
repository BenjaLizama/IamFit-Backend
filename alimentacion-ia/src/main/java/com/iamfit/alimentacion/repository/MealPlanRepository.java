package com.iamfit.alimentacion.repository;

import com.iamfit.alimentacion.entity.MealPlan;
import com.iamfit.alimentacion.entity.MealPlan.MealPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    List<MealPlan> findByUserIdOrderByCreatedAtDesc(String userId);

    List<MealPlan> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, MealPlanStatus status);

    Optional<MealPlan> findByUserIdAndStatus(String userId, MealPlanStatus status);

    Optional<MealPlan> findByIdAndUserId(UUID id, String userId);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, MealPlanStatus status);
}