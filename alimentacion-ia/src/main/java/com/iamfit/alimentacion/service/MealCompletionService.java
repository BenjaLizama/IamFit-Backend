package com.iamfit.alimentacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.alimentacion.dto.*;
import com.iamfit.alimentacion.entity.MealCompletion;
import com.iamfit.alimentacion.entity.MealPlan;
import com.iamfit.alimentacion.exception.*;
import com.iamfit.alimentacion.repository.MealCompletionRepository;
import com.iamfit.alimentacion.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealCompletionService {

    private final MealCompletionRepository mealCompletionRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> MEAL_SLOTS = List.of("desayuno", "almuerzo", "cena", "snacks");
    private static final List<String> WEEK_DAYS = List.of(
            "lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo");

    // ─── Consumir comida planificada ──────────────────────────────────

    @Transactional
    public MealCompletionResponse consumeMeal(String userId, UUID planId, String day,
                                              String mealId, ConsumeMealRequest request) {
        MealPlan plan = getOwnedPlan(userId, planId);
        validateActivePlan(plan);
        validateDayAndMeal(plan, day, mealId);

        LocalDate date = request.date() != null ? request.date() : LocalDate.now();

        MealCompletion completion = mealCompletionRepository
                .findByMealPlanIdAndLogDateAndMealType(planId, date, mealId)
                .orElseGet(() -> newCompletion(planId, userId, date, day, mealId));

        if (Boolean.TRUE.equals(completion.getCompleted())) {
            throw new MealAlreadyConsumedException("Esta comida ya fue marcada como consumida.");
        }

        completion.setCompleted(true);
        completion.setCompletedAt(Instant.now());
        mealCompletionRepository.save(completion);

        log.info("Comida consumida — plan: {}, dia: {}, comida: {}, fecha: {}",
                planId, day, mealId, date);

        boolean wantsLog = Boolean.TRUE.equals(request.createFoodLogEntries());

        return MealCompletionResponse.builder()
                .planId(planId).day(day).mealId(mealId)
                .completed(true).completedAt(completion.getCompletedAt())
                .createdFoodEntryIds(List.of())
                .warning(wantsLog
                        ? "El registro automatico en el diario de alimentos aun no esta disponible. La comida fue marcada como consumida."
                        : null)
                .build();
    }

    // ─── Deshacer consumo ──────────────────────────────────────────────

    @Transactional
    public MealCompletionResponse unconsumeMeal(String userId, UUID planId, String day, String mealId) {
        getOwnedPlan(userId, planId);

        MealCompletion completion = mealCompletionRepository
                .findByMealPlanIdAndUserId(planId, userId).stream()
                .filter(c -> c.getDayKey().equals(day) && c.getMealType().equals(mealId)
                        && Boolean.TRUE.equals(c.getCompleted()))
                .max(Comparator.comparing(MealCompletion::getCompletedAt))
                .orElseThrow(() -> new MealNotConsumedException(
                        "Esta comida no ha sido marcada como consumida."));

        completion.setCompleted(false);
        completion.setCompletedAt(null);
        mealCompletionRepository.save(completion);

        return MealCompletionResponse.builder()
                .planId(planId).day(day).mealId(mealId).completed(false).build();
    }

    // ─── Completar dia completo ─────────────────────────────────────────

    @Transactional
    public MealPlanDayCompleteResponse completeDay(String userId, UUID planId, String day,
                                                   CompleteMealPlanDayRequest request) {
        MealPlan plan = getOwnedPlan(userId, planId);
        validateActivePlan(plan);
        validateDay(plan, day);

        LocalDate date = request.date() != null ? request.date() : LocalDate.now();
        List<String> slots = getMealSlotsForDay(plan, day);

        for (String slot : slots) {
            MealCompletion completion = mealCompletionRepository
                    .findByMealPlanIdAndLogDateAndMealType(planId, date, slot)
                    .orElseGet(() -> newCompletion(planId, userId, date, day, slot));
            completion.setCompleted(true);
            completion.setCompletedAt(Instant.now());
            mealCompletionRepository.save(completion);
        }

        log.info("Dia completo marcado — plan: {}, dia: {}, fecha: {}", planId, day, date);

        return MealPlanDayCompleteResponse.builder()
                .planId(planId).day(day).completed(true).completedAt(Instant.now())
                .completedMeals(slots.size()).totalMeals(slots.size())
                .build();
    }

    // ─── Progreso del plan activo ───────────────────────────────────────

    @SuppressWarnings("unchecked")
    public MealPlanProgressResponse getActivePlanProgress(String userId) {
        MealPlan plan = mealPlanRepository
                .findByUserIdAndStatus(userId, MealPlan.MealPlanStatus.ACTIVE)
                .orElseThrow(() -> new MealPlanNotActiveException("No hay un plan activo."));

        Map<String, Object> menu = parseMenu(plan.getMenuJson());
        String currentDayKey = WEEK_DAYS.get(LocalDate.now().getDayOfWeek().getValue() - 1);

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        List<MealPlanDayProgress> daysProgress = new ArrayList<>();
        int totalSlots = 0;
        int completedSlots = 0;

        for (int i = 0; i < WEEK_DAYS.size(); i++) {
            String dayKey = WEEK_DAYS.get(i);
            if (!menu.containsKey(dayKey)) continue;

            Map<String, Object> dayMenu = (Map<String, Object>) menu.get(dayKey);
            List<String> slots = getSlotsPresent(dayMenu);
            LocalDate dateForDay = weekStart.plusDays(i);

            List<MealProgressItem> mealItems = new ArrayList<>();
            boolean allCompleted = !slots.isEmpty();
            Instant lastCompletedAt = null;

            for (String slot : slots) {
                totalSlots++;
                Optional<MealCompletion> completionOpt = mealCompletionRepository
                        .findByMealPlanIdAndLogDateAndMealType(plan.getId(), dateForDay, slot);

                boolean isCompleted = completionOpt.map(MealCompletion::getCompleted).orElse(false);
                if (isCompleted) {
                    completedSlots++;
                    lastCompletedAt = completionOpt.get().getCompletedAt();
                } else {
                    allCompleted = false;
                }

                mealItems.add(MealProgressItem.builder()
                        .mealId(slot).mealType(slot)
                        .title(extractTitle(dayMenu.get(slot)))
                        .completed(isCompleted)
                        .completedAt(completionOpt.map(MealCompletion::getCompletedAt).orElse(null))
                        .build());
            }

            daysProgress.add(MealPlanDayProgress.builder()
                    .day(dayKey).completed(allCompleted).completedAt(lastCompletedAt)
                    .meals(mealItems).build());
        }

        int progressPercentage = totalSlots > 0
                ? Math.round((completedSlots * 100f) / totalSlots) : 0;

        return MealPlanProgressResponse.builder()
                .planId(plan.getId()).name(plan.getTitle()).status(plan.getStatus().name())
                .currentDay(currentDayKey).progressPercentage(progressPercentage)
                .days(daysProgress)
                .build();
    }

    // ─── Historial ───────────────────────────────────────────────────────

    public MealPlanHistoryResponse getHistory(String userId, LocalDate from, LocalDate to) {
        List<MealPlan> plans = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (plans.isEmpty()) {
            return MealPlanHistoryResponse.builder().from(from).to(to).days(List.of()).build();
        }

        List<UUID> planIds = plans.stream().map(MealPlan::getId).toList();
        List<MealCompletion> completions = mealCompletionRepository
                .findByMealPlanIdInAndLogDateBetween(planIds, from, to);

        Map<LocalDate, List<MealCompletion>> byDate = completions.stream()
                .collect(Collectors.groupingBy(MealCompletion::getLogDate));

        List<MealPlanHistoryDay> days = byDate.entrySet().stream()
                .map(e -> {
                    long completedCount = e.getValue().stream()
                            .filter(MealCompletion::getCompleted).count();
                    int total = e.getValue().size();
                    return MealPlanHistoryDay.builder()
                            .date(e.getKey())
                            .planId(e.getValue().get(0).getMealPlanId())
                            .completedMeals((int) completedCount)
                            .totalMeals(total)
                            .completed(completedCount == total && total > 0)
                            .adherencePercentage(total > 0
                                    ? Math.round((completedCount * 100f) / total) : 0)
                            .build();
                })
                .sorted(Comparator.comparing(MealPlanHistoryDay::getDate))
                .toList();

        return MealPlanHistoryResponse.builder().from(from).to(to).days(days).build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private MealPlan getOwnedPlan(String userId, UUID planId) {
        return mealPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));
    }

    private void validateActivePlan(MealPlan plan) {
        if (plan.getStatus() != MealPlan.MealPlanStatus.ACTIVE) {
            throw new MealPlanNotActiveException("El plan de comidas no esta activo.");
        }
    }

    @SuppressWarnings("unchecked")
    private void validateDayAndMeal(MealPlan plan, String day, String mealType) {
        Map<String, Object> menu = parseMenu(plan.getMenuJson());
        if (!menu.containsKey(day)) {
            throw new MealPlanDayNotFoundException("El dia '" + day + "' no existe en este plan.");
        }
        Map<String, Object> dayMenu = (Map<String, Object>) menu.get(day);
        if (!dayMenu.containsKey(mealType)) {
            throw new MealPlanDayNotFoundException(
                    "La comida '" + mealType + "' no existe para el dia '" + day + "'.");
        }
    }

    @SuppressWarnings("unchecked")
    private void validateDay(MealPlan plan, String day) {
        Map<String, Object> menu = parseMenu(plan.getMenuJson());
        if (!menu.containsKey(day)) {
            throw new MealPlanDayNotFoundException("El dia '" + day + "' no existe en este plan.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getMealSlotsForDay(MealPlan plan, String day) {
        Map<String, Object> menu = parseMenu(plan.getMenuJson());
        Map<String, Object> dayMenu = (Map<String, Object>) menu.get(day);
        return getSlotsPresent(dayMenu);
    }

    private List<String> getSlotsPresent(Map<String, Object> dayMenu) {
        if (dayMenu == null) return List.of();
        return MEAL_SLOTS.stream().filter(dayMenu::containsKey).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMenu(String menuJson) {
        if (menuJson == null) return Map.of();
        try {
            return objectMapper.readValue(menuJson, Map.class);
        } catch (Exception e) {
            log.warn("No se pudo parsear el menu del plan: {}", e.getMessage());
            return Map.of();
        }
    }

    private String extractTitle(Object mealValue) {
        if (mealValue instanceof String s) return s;
        if (mealValue instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        return mealValue != null ? mealValue.toString() : "";
    }

    private MealCompletion newCompletion(UUID planId, String userId, LocalDate date,
                                         String day, String mealType) {
        MealCompletion c = new MealCompletion();
        c.setMealPlanId(planId);
        c.setUserId(userId);
        c.setLogDate(date);
        c.setDayKey(day);
        c.setMealType(mealType);
        return c;
    }
}