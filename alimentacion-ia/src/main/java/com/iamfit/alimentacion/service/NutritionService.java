package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.FoodEntryDto;
import com.iamfit.alimentacion.dto.NutritionSummaryDto;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import com.iamfit.alimentacion.repository.DailyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionService {

    private final DailyLogRepository dailyLogRepository;
    private final FoodLogService foodLogService;

    // ─── Resumen nutricional del día ─────────────────────────────────

    public NutritionSummaryDto getDailySummary(String userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<FoodEntryDto> entries = foodLogService.getEntriesForDay(userId, targetDate);

        // Totales del día
        double totalCalories    = sum(entries, FoodEntryDto::getCalories);
        double totalProtein     = sum(entries, FoodEntryDto::getProtein);
        double totalCarbs       = sum(entries, FoodEntryDto::getCarbohydrates);
        double totalFat         = sum(entries, FoodEntryDto::getFat);
        double totalFiber       = sum(entries, FoodEntryDto::getFiber);

        // Agrupación por tipo de comida
        Map<MealType, List<FoodEntryDto>> entriesByMeal = entries.stream()
                .collect(Collectors.groupingBy(
                        FoodEntryDto::getMealType,
                        () -> new EnumMap<>(MealType.class),
                        Collectors.toList()
                ));

        // Totales por tipo de comida
        Map<MealType, NutritionSummaryDto.MealNutritionDto> mealTotals =
                new EnumMap<>(MealType.class);

        for (MealType mealType : MealType.values()) {
            List<FoodEntryDto> mealEntries = entriesByMeal.getOrDefault(mealType, List.of());
            mealTotals.put(mealType, NutritionSummaryDto.MealNutritionDto.builder()
                    .calories(sum(mealEntries, FoodEntryDto::getCalories))
                    .protein(sum(mealEntries, FoodEntryDto::getProtein))
                    .carbohydrates(sum(mealEntries, FoodEntryDto::getCarbohydrates))
                    .fat(sum(mealEntries, FoodEntryDto::getFat))
                    .fiber(sum(mealEntries, FoodEntryDto::getFiber))
                    .build());
        }

        return NutritionSummaryDto.builder()
                .date(targetDate)
                .userId(userId)
                .totalCalories(round(totalCalories))
                .totalProtein(round(totalProtein))
                .totalCarbohydrates(round(totalCarbs))
                .totalFat(round(totalFat))
                .totalFiber(round(totalFiber))
                .entriesByMeal(entriesByMeal)
                .mealTotals(mealTotals)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private double sum(List<FoodEntryDto> entries,
                       java.util.function.Function<FoodEntryDto, Double> getter) {
        return entries.stream()
                .mapToDouble(e -> {
                    Double val = getter.apply(e);
                    return val != null ? val : 0.0;
                })
                .sum();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}