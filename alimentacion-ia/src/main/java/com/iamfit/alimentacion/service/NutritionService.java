package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.FoodEntryDto;
import com.iamfit.alimentacion.dto.FoodLimitsDto;
import com.iamfit.alimentacion.dto.NutritionSummaryDto;
import com.iamfit.alimentacion.entity.DailyLog;
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

    public FoodLimitsDto getFoodLimits(String userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        long entries = dailyLogRepository
                .findByUserIdAndLogDate(userId, targetDate)
                .map(log -> (long) log.getEntries().size())
                .orElse(0L);
        int max = 50;
        return FoodLimitsDto.builder()
                .maxFoodEntriesPerDay(max)
                .entriesForDate(entries)
                .canAddFood(entries < max)
                .build();
    }

    public List<Map<String, Object>> getMonthlyCaloriesSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);

        java.util.Map<String, Double> caloriesByMonth = new java.util.LinkedHashMap<>();

        for (DailyLog log : logs) {
            if (log.getLogDate() != null) {
                String month = log.getLogDate().toString().substring(0, 7);
                double totalCalories = log.getEntries().stream()
                        .mapToDouble(e -> e.getCalculatedCalories() != null
                                ? e.getCalculatedCalories() : 0.0)
                        .sum();
                caloriesByMonth.merge(month, totalCalories, Double::sum);
            }
        }

        return caloriesByMonth.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "month", e.getKey(),
                        "totalCalories", Math.round(e.getValue() * 100.0) / 100.0
                ))
                .toList();
    }

    public List<Map<String, Object>> getMonthlyProteinSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);

        java.util.Map<String, Double> proteinByMonth = new java.util.LinkedHashMap<>();

        for (DailyLog log : logs) {
            if (log.getLogDate() != null) {
                String month = log.getLogDate().toString().substring(0, 7);
                double totalProtein = log.getEntries().stream()
                        .mapToDouble(e -> e.getCalculatedProtein() != null
                                ? e.getCalculatedProtein() : 0.0)
                        .sum();
                proteinByMonth.merge(month, totalProtein, Double::sum);
            }
        }

        return proteinByMonth.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "month", e.getKey(),
                        "totalProtein", Math.round(e.getValue() * 100.0) / 100.0
                ))
                .toList();
    }

    public List<Map<String, Object>> getDailyCaloriesSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(30);

        return logs.stream()
                .filter(log -> log.getLogDate() != null && !log.getLogDate().isBefore(cutoff))
                .map(log -> {
                    double total = log.getEntries().stream()
                            .mapToDouble(e -> e.getCalculatedCalories() != null ? e.getCalculatedCalories() : 0.0)
                            .sum();
                    return Map.<String, Object>of(
                            "date", log.getLogDate().toString(),
                            "totalCalories", Math.round(total * 100.0) / 100.0
                    );
                })
                .toList();
    }

    public List<Map<String, Object>> getWeeklyCaloriesSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusWeeks(12);

        java.util.Map<String, Double> byWeek = new java.util.LinkedHashMap<>();
        for (DailyLog log : logs) {
            if (log.getLogDate() == null || log.getLogDate().isBefore(cutoff)) continue;
            int week = log.getLogDate().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            String key = "W" + String.format("%02d", week);
            double total = log.getEntries().stream()
                    .mapToDouble(e -> e.getCalculatedCalories() != null ? e.getCalculatedCalories() : 0.0)
                    .sum();
            byWeek.merge(key, total, Double::sum);
        }

        return byWeek.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "week", e.getKey(),
                        "totalCalories", Math.round(e.getValue() * 100.0) / 100.0
                ))
                .toList();
    }

    public List<Map<String, Object>> getDailyProteinSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(30);

        return logs.stream()
                .filter(log -> log.getLogDate() != null && !log.getLogDate().isBefore(cutoff))
                .map(log -> {
                    double total = log.getEntries().stream()
                            .mapToDouble(e -> e.getCalculatedProtein() != null ? e.getCalculatedProtein() : 0.0)
                            .sum();
                    return Map.<String, Object>of(
                            "date", log.getLogDate().toString(),
                            "totalProtein", Math.round(total * 100.0) / 100.0
                    );
                })
                .toList();
    }

    public List<Map<String, Object>> getWeeklyProteinSummary(String userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByLogDate(userId);
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusWeeks(12);

        java.util.Map<String, Double> byWeek = new java.util.LinkedHashMap<>();
        for (DailyLog log : logs) {
            if (log.getLogDate() == null || log.getLogDate().isBefore(cutoff)) continue;
            int week = log.getLogDate().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            String key = "W" + String.format("%02d", week);
            double total = log.getEntries().stream()
                    .mapToDouble(e -> e.getCalculatedProtein() != null ? e.getCalculatedProtein() : 0.0)
                    .sum();
            byWeek.merge(key, total, Double::sum);
        }

        return byWeek.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "week", e.getKey(),
                        "totalProtein", Math.round(e.getValue() * 100.0) / 100.0
                ))
                .toList();
    }
}