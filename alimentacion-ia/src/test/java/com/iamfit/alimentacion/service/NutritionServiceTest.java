package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.FoodEntryDto;
import com.iamfit.alimentacion.dto.FoodLimitsDto;
import com.iamfit.alimentacion.dto.NutritionSummaryDto;
import com.iamfit.alimentacion.entity.DailyLog;
import com.iamfit.alimentacion.entity.FoodEntry;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import com.iamfit.alimentacion.repository.DailyLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private DailyLogRepository dailyLogRepository;
    @Mock
    private FoodLogService foodLogService;

    @InjectMocks
    private NutritionService service;

    private final String userId = "user-1";

    @Test
    void getDailySummary_aggregatesTotalsAndPerMeal() {
        FoodEntryDto breakfast = FoodEntryDto.builder()
                .mealType(MealType.DESAYUNO)
                .calories(100.0).protein(10.0).carbohydrates(20.0).fat(5.0).fiber(2.0)
                .build();
        FoodEntryDto lunch = FoodEntryDto.builder()
                .mealType(MealType.ALMUERZO)
                .calories(200.0).protein(20.0).carbohydrates(40.0).fat(10.0).fiber(null)
                .build();
        when(foodLogService.getEntriesForDay(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(breakfast, lunch));

        NutritionSummaryDto summary = service.getDailySummary(userId, LocalDate.of(2024, 1, 1));

        assertThat(summary.getTotalCalories()).isEqualTo(300.0);
        assertThat(summary.getTotalProtein()).isEqualTo(30.0);
        assertThat(summary.getTotalFiber()).isEqualTo(2.0);
        assertThat(summary.getMealTotals().get(MealType.DESAYUNO).getCalories()).isEqualTo(100.0);
        assertThat(summary.getMealTotals().get(MealType.ALMUERZO).getCalories()).isEqualTo(200.0);
        assertThat(summary.getMealTotals().get(MealType.CENA).getCalories()).isEqualTo(0.0);
    }

    @Test
    void getDailySummary_usesTodayWhenDateNull() {
        when(foodLogService.getEntriesForDay(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        NutritionSummaryDto summary = service.getDailySummary(userId, null);

        assertThat(summary.getDate()).isEqualTo(LocalDate.now());
        assertThat(summary.getTotalCalories()).isEqualTo(0.0);
    }

    @Test
    void getFoodLimits_withExistingEntries() {
        DailyLog log = new DailyLog();
        log.setUserId(userId);
        log.getEntries().add(new FoodEntry());
        log.getEntries().add(new FoodEntry());
        when(dailyLogRepository.findByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(log));

        FoodLimitsDto limits = service.getFoodLimits(userId, LocalDate.now());

        assertThat(limits.getMaxFoodEntriesPerDay()).isEqualTo(50);
        assertThat(limits.getEntriesForDate()).isEqualTo(2);
        assertThat(limits.getCanAddFood()).isTrue();
    }

    @Test
    void getFoodLimits_noLogReturnsZero() {
        when(dailyLogRepository.findByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        FoodLimitsDto limits = service.getFoodLimits(userId, null);

        assertThat(limits.getEntriesForDate()).isEqualTo(0);
        assertThat(limits.getCanAddFood()).isTrue();
    }
}
