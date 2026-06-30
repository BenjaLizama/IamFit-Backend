package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.AddFoodRequest;
import com.iamfit.alimentacion.dto.DeleteFoodEntryResponse;
import com.iamfit.alimentacion.dto.EditFoodEntryRequest;
import com.iamfit.alimentacion.dto.FoodEntryDto;
import com.iamfit.alimentacion.entity.DailyLog;
import com.iamfit.alimentacion.entity.FoodEntry;
import com.iamfit.alimentacion.entity.FoodEntry.MealType;
import com.iamfit.alimentacion.entity.FoodItem;
import com.iamfit.alimentacion.repository.DailyLogRepository;
import com.iamfit.alimentacion.repository.FoodEntryRepository;
import com.iamfit.alimentacion.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodLogServiceTest {

    @Mock
    private DailyLogRepository dailyLogRepository;
    @Mock
    private FoodEntryRepository foodEntryRepository;
    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private FoodLogService service;

    private FoodItem foodItem;
    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        foodItem = new FoodItem();
        foodItem.setId(UUID.randomUUID());
        foodItem.setName("Arroz");
        foodItem.setCalories(130.0);
        foodItem.setProtein(2.7);
        foodItem.setCarbohydrates(28.0);
        foodItem.setFat(0.3);
        foodItem.setFiber(0.4);
    }

    private AddFoodRequest addRequest(double qty, LocalDate date) {
        AddFoodRequest req = new AddFoodRequest();
        req.setFoodItemId(foodItem.getId());
        req.setQuantity(qty);
        req.setMealType(MealType.ALMUERZO);
        req.setLogDate(date);
        return req;
    }

    @Test
    void addFood_createsNewDailyLogAndCalculatesMacros() {
        AddFoodRequest req = addRequest(200.0, LocalDate.of(2024, 1, 1));
        when(foodItemRepository.findById(foodItem.getId())).thenReturn(Optional.of(foodItem));
        when(dailyLogRepository.findByUserIdAndLogDate(userId, req.getLogDate()))
                .thenReturn(Optional.empty());
        when(dailyLogRepository.save(any(DailyLog.class))).thenAnswer(i -> i.getArgument(0));
        when(foodEntryRepository.save(any(FoodEntry.class))).thenAnswer(i -> i.getArgument(0));

        FoodEntryDto dto = service.addFood(userId, req);

        // factor = 2.0
        assertThat(dto.getCalories()).isEqualTo(260.0);
        assertThat(dto.getProtein()).isEqualTo(5.4);
        assertThat(dto.getFiber()).isEqualTo(0.8);
        assertThat(dto.getFoodName()).isEqualTo("Arroz");
        verify(dailyLogRepository).save(any(DailyLog.class));
    }

    @Test
    void addFood_usesExistingDailyLog_andNullFiber() {
        foodItem.setFiber(null);
        AddFoodRequest req = addRequest(100.0, null);
        DailyLog existing = new DailyLog();
        existing.setUserId(userId);
        when(foodItemRepository.findById(foodItem.getId())).thenReturn(Optional.of(foodItem));
        when(dailyLogRepository.findByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(foodEntryRepository.save(any(FoodEntry.class))).thenAnswer(i -> i.getArgument(0));

        FoodEntryDto dto = service.addFood(userId, req);

        assertThat(dto.getFiber()).isNull();
        verify(dailyLogRepository, never()).save(any());
    }

    @Test
    void addFood_throwsWhenFoodNotFound() {
        AddFoodRequest req = addRequest(100.0, null);
        when(foodItemRepository.findById(foodItem.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addFood(userId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alimento no encontrado");
    }

    @Test
    void deleteFood_deletesWhenOwned() {
        UUID entryId = UUID.randomUUID();
        DailyLog log = new DailyLog();
        log.setUserId(userId);
        FoodEntry entry = new FoodEntry();
        entry.setId(entryId);
        entry.setDailyLog(log);
        when(foodEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        DeleteFoodEntryResponse resp = service.deleteFood(userId, entryId);

        assertThat(resp.getDeletedId()).isEqualTo(entryId);
        verify(foodEntryRepository).delete(entry);
    }

    @Test
    void deleteFood_throwsWhenNotOwner() {
        UUID entryId = UUID.randomUUID();
        DailyLog log = new DailyLog();
        log.setUserId("other");
        FoodEntry entry = new FoodEntry();
        entry.setDailyLog(log);
        when(foodEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.deleteFood(userId, entryId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
        verify(foodEntryRepository, never()).delete(any());
    }

    @Test
    void deleteFood_throwsWhenNotFound() {
        UUID entryId = UUID.randomUUID();
        when(foodEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFood(userId, entryId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Registro no encontrado");
    }

    @Test
    void getEntriesForDay_returnsMappedEntries() {
        FoodEntry entry = new FoodEntry();
        entry.setId(UUID.randomUUID());
        entry.setFoodItem(foodItem);
        entry.setQuantity(100.0);
        entry.setMealType(MealType.CENA);
        entry.setCalculatedCalories(130.0);
        DailyLog log = new DailyLog();
        log.setUserId(userId);
        log.getEntries().add(entry);
        when(dailyLogRepository.findByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(log));

        List<FoodEntryDto> result = service.getEntriesForDay(userId, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMealType()).isEqualTo(MealType.CENA);
    }

    @Test
    void getEntriesForDay_returnsEmptyWhenNoLog() {
        when(dailyLogRepository.findByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThat(service.getEntriesForDay(userId, LocalDate.now())).isEmpty();
    }

    @Test
    void editFood_updatesQuantityAndMealType() {
        UUID entryId = UUID.randomUUID();
        DailyLog log = new DailyLog();
        log.setUserId(userId);
        FoodEntry entry = new FoodEntry();
        entry.setId(entryId);
        entry.setDailyLog(log);
        entry.setFoodItem(foodItem);
        entry.setQuantity(100.0);
        entry.setMealType(MealType.DESAYUNO);
        when(foodEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));
        when(foodEntryRepository.save(any(FoodEntry.class))).thenAnswer(i -> i.getArgument(0));

        EditFoodEntryRequest req = new EditFoodEntryRequest();
        req.setQuantity(200.0);
        req.setMealType(MealType.CENA);

        FoodEntryDto dto = service.editFood(userId, entryId, req);

        assertThat(dto.getQuantity()).isEqualTo(200.0);
        assertThat(dto.getMealType()).isEqualTo(MealType.CENA);
        assertThat(dto.getCalories()).isEqualTo(260.0);
    }

    @Test
    void editFood_throwsWhenNotOwner() {
        UUID entryId = UUID.randomUUID();
        DailyLog log = new DailyLog();
        log.setUserId("other");
        FoodEntry entry = new FoodEntry();
        entry.setDailyLog(log);
        when(foodEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.editFood(userId, entryId, new EditFoodEntryRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }
}
