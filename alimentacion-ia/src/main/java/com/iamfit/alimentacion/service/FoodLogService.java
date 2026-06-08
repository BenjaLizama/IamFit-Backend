package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.AddFoodRequest;
import com.iamfit.alimentacion.dto.DeleteFoodEntryResponse;
import com.iamfit.alimentacion.dto.EditFoodEntryRequest;
import com.iamfit.alimentacion.dto.FoodEntryDto;
import com.iamfit.alimentacion.entity.DailyLog;
import com.iamfit.alimentacion.entity.FoodEntry;
import com.iamfit.alimentacion.entity.FoodItem;
import com.iamfit.alimentacion.repository.DailyLogRepository;
import com.iamfit.alimentacion.repository.FoodEntryRepository;
import com.iamfit.alimentacion.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodLogService {

    private final DailyLogRepository dailyLogRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final FoodItemRepository foodItemRepository;

    // ─── Registrar alimento ──────────────────────────────────────────

    @Transactional
    public FoodEntryDto addFood(String userId, AddFoodRequest request) {
        LocalDate date = request.getLogDate() != null ? request.getLogDate() : LocalDate.now();

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new RuntimeException(
                        "Alimento no encontrado: " + request.getFoodItemId()));

        DailyLog dailyLog = dailyLogRepository
                .findByUserIdAndLogDate(userId, date)
                .orElseGet(() -> createDailyLog(userId, date));

        FoodEntry entry = new FoodEntry();
        entry.setDailyLog(dailyLog);  // ← usar dailyLog en vez de log
        entry.setFoodItem(foodItem);
        entry.setQuantity(request.getQuantity());
        entry.setMealType(request.getMealType());

        double factor = request.getQuantity() / 100.0;
        entry.setCalculatedCalories(round(foodItem.getCalories() * factor));
        entry.setCalculatedProtein(round(foodItem.getProtein() * factor));
        entry.setCalculatedCarbs(round(foodItem.getCarbohydrates() * factor));
        entry.setCalculatedFat(round(foodItem.getFat() * factor));
        entry.setCalculatedFiber(foodItem.getFiber() != null
                ? round(foodItem.getFiber() * factor) : null);

        FoodEntry saved = foodEntryRepository.save(entry);
        log.info("Alimento registrado — usuario: {}, alimento: {}, cantidad: {}g, comida: {}",
                userId, foodItem.getName(), request.getQuantity(), request.getMealType());

        return toDto(saved);
    }

    // ─── Eliminar registro ───────────────────────────────────────────

    @Transactional
    public DeleteFoodEntryResponse deleteFood(String userId, UUID entryId) {
        FoodEntry entry = foodEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado: " + entryId));

        // Verificar que el registro pertenece al usuario
        DailyLog dailyLog = entry.getDailyLog();
        if (!dailyLog.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este registro");
        }

        foodEntryRepository.delete(entry);
        log.info("Registro eliminado — id: {}, usuario: {}", entryId, userId);

        return DeleteFoodEntryResponse.builder()
                .deletedId(entryId)
                .message("Alimento eliminado correctamente")
                .build();
    }

    // ─── Obtener entradas del día ────────────────────────────────────

    public List<FoodEntryDto> getEntriesForDay(String userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return dailyLogRepository
                .findByUserIdAndLogDate(userId, targetDate)
                .map(dailyLog -> dailyLog.getEntries().stream()
                        .map(this::toDto)
                        .toList())
                .orElse(List.of());
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private DailyLog createDailyLog(String userId, LocalDate date) {
        DailyLog newLog = new DailyLog();
        newLog.setUserId(userId);
        newLog.setLogDate(date);
        return dailyLogRepository.save(newLog);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public FoodEntryDto toDto(FoodEntry entry) {
        return FoodEntryDto.builder()
                .id(entry.getId())
                .foodName(entry.getFoodItem().getName())
                .quantity(entry.getQuantity())
                .mealType(entry.getMealType())
                .calories(entry.getCalculatedCalories())
                .protein(entry.getCalculatedProtein())
                .carbohydrates(entry.getCalculatedCarbs())
                .fat(entry.getCalculatedFat())
                .fiber(entry.getCalculatedFiber())
                .build();
    }

    @Transactional
    public FoodEntryDto editFood(String userId, UUID entryId, EditFoodEntryRequest request) {
        FoodEntry entry = foodEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado: " + entryId));

        if (!entry.getDailyLog().getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar este registro");
        }

        FoodItem foodItem = entry.getFoodItem();

        if (request.getQuantity() != null && request.getQuantity() > 0) {
            entry.setQuantity(request.getQuantity());
            double factor = request.getQuantity() / 100.0;
            entry.setCalculatedCalories(round(foodItem.getCalories() * factor));
            entry.setCalculatedProtein(round(foodItem.getProtein() * factor));
            entry.setCalculatedCarbs(round(foodItem.getCarbohydrates() * factor));
            entry.setCalculatedFat(round(foodItem.getFat() * factor));
            entry.setCalculatedFiber(foodItem.getFiber() != null
                    ? round(foodItem.getFiber() * factor) : null);
        }

        if (request.getMealType() != null) {
            entry.setMealType(request.getMealType());
        }

        FoodEntry saved = foodEntryRepository.save(entry);
        log.info("Entrada editada — id: {}, usuario: {}", entryId, userId);
        return toDto(saved);
    }
}