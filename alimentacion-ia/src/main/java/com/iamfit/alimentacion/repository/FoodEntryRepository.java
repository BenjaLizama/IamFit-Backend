package com.iamfit.alimentacion.repository;

import com.iamfit.alimentacion.entity.FoodEntry;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodEntryRepository extends JpaRepository<FoodEntry, UUID> {

    List<FoodEntry> findByDailyLogId(UUID dailyLogId);

    List<FoodEntry> findByDailyLogIdAndMealType(UUID dailyLogId, MealType mealType);

    @Query("SELECT fe FROM FoodEntry fe " +
            "JOIN fe.dailyLog dl " +
            "WHERE dl.userId = :userId AND dl.logDate = CURRENT_DATE")
    List<FoodEntry> findTodayEntriesByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(fe.calculatedCalories), 0) FROM FoodEntry fe " +
            "JOIN fe.dailyLog dl " +
            "WHERE dl.id = :logId")
    Double sumCaloriesByLogId(@Param("logId") UUID logId);
}