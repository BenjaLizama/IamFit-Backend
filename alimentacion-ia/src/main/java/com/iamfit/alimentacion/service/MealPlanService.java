package com.iamfit.alimentacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.alimentacion.dto.*;
import com.iamfit.alimentacion.entity.MealPlan;
import com.iamfit.alimentacion.entity.MealPlan.MealPlanStatus;
import com.iamfit.alimentacion.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_SAVED = 10;
    private static final int MAX_ACTIVE = 1;

    // ─── Guardar plan generado ───────────────────────────────────────

    @Transactional
    public MealPlanDto saveMealPlan(String userId, SaveMealPlanRequest request) {
        long total = mealPlanRepository.countByUserId(userId);
        if (total >= MAX_SAVED) {
            throw new IllegalStateException(
                    "Has alcanzado el limite de " + MAX_SAVED + " planes guardados. Elimina uno antes de guardar otro.");
        }

        MealPlan plan = new MealPlan();
        plan.setUserId(userId);
        plan.setTitle(request.getTitle());
        plan.setGoal(request.getGoal());
        plan.setRecommendations(request.getRecomendacionesNutricionales());
        plan.setSource(MealPlan.MealPlanSource.AI);
        plan.setStatus(MealPlanStatus.INACTIVE);

        try {
            if (request.getMenu() != null) {
                plan.setMenuJson(objectMapper.writeValueAsString(request.getMenu()));
            }
        } catch (Exception e) {
            log.warn("No se pudo serializar el menu: {}", e.getMessage());
        }

        MealPlan saved = mealPlanRepository.save(plan);
        log.info("Plan de comidas guardado — id: {}, usuario: {}", saved.getId(), userId);
        return toDto(saved);
    }

    // ─── Listar planes ───────────────────────────────────────────────

    public List<MealPlanDto> getMealPlans(String userId, String status) {
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> mealPlanRepository
                    .findByUserIdAndStatusOrderByCreatedAtDesc(userId, MealPlanStatus.ACTIVE)
                    .stream().map(this::toDto).toList();
            case "INACTIVE" -> mealPlanRepository
                    .findByUserIdAndStatusOrderByCreatedAtDesc(userId, MealPlanStatus.INACTIVE)
                    .stream().map(this::toDto).toList();
            default -> mealPlanRepository
                    .findByUserIdOrderByCreatedAtDesc(userId)
                    .stream().map(this::toDto).toList();
        };
    }

    // ─── Plan activo ─────────────────────────────────────────────────

    public MealPlanDto getActiveMealPlan(String userId) {
        return mealPlanRepository.findByUserIdAndStatus(userId, MealPlanStatus.ACTIVE)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("No hay un plan activo"));
    }

    // ─── Activar plan ────────────────────────────────────────────────

    @Transactional
    public MealPlanDto activateMealPlan(String userId, UUID planId) {
        MealPlan plan = mealPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));

        // Desactivar el plan activo anterior si existe
        mealPlanRepository.findByUserIdAndStatus(userId, MealPlanStatus.ACTIVE)
                .ifPresent(current -> {
                    current.setStatus(MealPlanStatus.INACTIVE);
                    current.setDeactivatedAt(LocalDateTime.now());
                    mealPlanRepository.save(current);
                    log.info("Plan anterior desactivado — id: {}", current.getId());
                });

        plan.setStatus(MealPlanStatus.ACTIVE);
        plan.setActivatedAt(LocalDateTime.now());
        MealPlan saved = mealPlanRepository.save(plan);
        log.info("Plan activado — id: {}, usuario: {}", saved.getId(), userId);
        return toDto(saved);
    }

    // ─── Desactivar plan ─────────────────────────────────────────────

    @Transactional
    public MealPlanDto deactivateMealPlan(String userId, UUID planId) {
        MealPlan plan = mealPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));
        plan.setStatus(MealPlanStatus.INACTIVE);
        plan.setDeactivatedAt(LocalDateTime.now());
        log.info("Plan desactivado — id: {}, usuario: {}", planId, userId);
        return toDto(mealPlanRepository.save(plan));
    }

    // ─── Eliminar plan ───────────────────────────────────────────────

    @Transactional
    public void deleteMealPlan(String userId, UUID planId) {
        MealPlan plan = mealPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));
        mealPlanRepository.delete(plan);
        log.info("Plan eliminado — id: {}, usuario: {}", planId, userId);
    }

    // ─── Limites ─────────────────────────────────────────────────────

    public MealPlanLimitsDto getLimits(String userId) {
        long saved = mealPlanRepository.countByUserId(userId);
        long active = mealPlanRepository.countByUserIdAndStatus(userId, MealPlanStatus.ACTIVE);
        return MealPlanLimitsDto.builder()
                .maxSavedMealPlans(MAX_SAVED)
                .maxActiveMealPlans(MAX_ACTIVE)
                .savedMealPlans(saved)
                .activeMealPlans(active)
                .canSaveMealPlan(saved < MAX_SAVED)
                .canActivateMealPlan(true)
                .build();
    }

    // ─── Mapper ──────────────────────────────────────────────────────

    private MealPlanDto toDto(MealPlan plan) {
        MealPlanResponse menuParsed = null;
        if (plan.getMenuJson() != null) {
            try {
                menuParsed = objectMapper.readValue(plan.getMenuJson(), MealPlanResponse.class);
            } catch (Exception e) {
                log.warn("No se pudo deserializar menu — id: {}", plan.getId());
            }
        }
        return MealPlanDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .goal(plan.getGoal())
                .status(plan.getStatus())
                .source(plan.getSource())
                .menu(menuParsed)
                .createdAt(plan.getCreatedAt())
                .activatedAt(plan.getActivatedAt())
                .build();
    }
}