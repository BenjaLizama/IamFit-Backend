package com.iamfit.alimentacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.alimentacion.dto.MealPlanDto;
import com.iamfit.alimentacion.dto.MealPlanLimitsDto;
import com.iamfit.alimentacion.dto.SaveMealPlanRequest;
import com.iamfit.alimentacion.entity.MealPlan;
import com.iamfit.alimentacion.entity.MealPlan.MealPlanStatus;
import com.iamfit.alimentacion.repository.MealPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    private MealPlanService service;

    private final String userId = "user-1";

    @BeforeEach
    void setUp() {
        service = new MealPlanService(mealPlanRepository, new ObjectMapper());
    }

    private MealPlan plan(UUID id, MealPlanStatus status) {
        MealPlan p = new MealPlan();
        p.setId(id);
        p.setUserId(userId);
        p.setTitle("Plan");
        p.setGoal("Ganar musculo");
        p.setStatus(status);
        p.setSource(MealPlan.MealPlanSource.AI);
        return p;
    }

    @Test
    void saveMealPlan_savesAndReturnsDto() {
        when(mealPlanRepository.countByUserId(userId)).thenReturn(0L);
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(i -> i.getArgument(0));

        SaveMealPlanRequest req = new SaveMealPlanRequest();
        req.setTitle("Mi plan");
        req.setGoal("Ganar musculo");

        MealPlanDto dto = service.saveMealPlan(userId, req);

        assertThat(dto.getTitle()).isEqualTo("Mi plan");
        assertThat(dto.getStatus()).isEqualTo(MealPlanStatus.INACTIVE);
        verify(mealPlanRepository).save(any(MealPlan.class));
    }

    @Test
    void saveMealPlan_throwsWhenLimitReached() {
        when(mealPlanRepository.countByUserId(userId)).thenReturn(10L);

        assertThatThrownBy(() -> service.saveMealPlan(userId, new SaveMealPlanRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limite");
        verify(mealPlanRepository, never()).save(any());
    }

    @Test
    void getMealPlans_active() {
        when(mealPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MealPlanStatus.ACTIVE))
                .thenReturn(List.of(plan(UUID.randomUUID(), MealPlanStatus.ACTIVE)));

        List<MealPlanDto> result = service.getMealPlans(userId, "active");

        assertThat(result).hasSize(1);
    }

    @Test
    void getMealPlans_inactive() {
        when(mealPlanRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MealPlanStatus.INACTIVE))
                .thenReturn(List.of());

        assertThat(service.getMealPlans(userId, "INACTIVE")).isEmpty();
    }

    @Test
    void getMealPlans_all() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(plan(UUID.randomUUID(), MealPlanStatus.INACTIVE)));

        assertThat(service.getMealPlans(userId, "ALL")).hasSize(1);
    }

    @Test
    void getActiveMealPlan_returnsDto() {
        when(mealPlanRepository.findByUserIdAndStatus(userId, MealPlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan(UUID.randomUUID(), MealPlanStatus.ACTIVE)));

        assertThat(service.getActiveMealPlan(userId)).isNotNull();
    }

    @Test
    void getActiveMealPlan_throwsWhenNone() {
        when(mealPlanRepository.findByUserIdAndStatus(userId, MealPlanStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveMealPlan(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay un plan activo");
    }

    @Test
    void activateMealPlan_deactivatesPreviousAndActivates() {
        UUID planId = UUID.randomUUID();
        MealPlan target = plan(planId, MealPlanStatus.INACTIVE);
        MealPlan previous = plan(UUID.randomUUID(), MealPlanStatus.ACTIVE);
        when(mealPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(target));
        when(mealPlanRepository.findByUserIdAndStatus(userId, MealPlanStatus.ACTIVE))
                .thenReturn(Optional.of(previous));
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(i -> i.getArgument(0));

        MealPlanDto dto = service.activateMealPlan(userId, planId);

        assertThat(dto.getStatus()).isEqualTo(MealPlanStatus.ACTIVE);
        assertThat(previous.getStatus()).isEqualTo(MealPlanStatus.INACTIVE);
    }

    @Test
    void activateMealPlan_throwsWhenNotFound() {
        UUID planId = UUID.randomUUID();
        when(mealPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateMealPlan(userId, planId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Plan no encontrado");
    }

    @Test
    void deactivateMealPlan_setsInactive() {
        UUID planId = UUID.randomUUID();
        MealPlan target = plan(planId, MealPlanStatus.ACTIVE);
        when(mealPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(target));
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(i -> i.getArgument(0));

        MealPlanDto dto = service.deactivateMealPlan(userId, planId);

        assertThat(dto.getStatus()).isEqualTo(MealPlanStatus.INACTIVE);
    }

    @Test
    void deleteMealPlan_deletes() {
        UUID planId = UUID.randomUUID();
        MealPlan target = plan(planId, MealPlanStatus.INACTIVE);
        when(mealPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(target));

        service.deleteMealPlan(userId, planId);

        verify(mealPlanRepository).delete(target);
    }

    @Test
    void deleteMealPlan_throwsWhenNotFound() {
        UUID planId = UUID.randomUUID();
        when(mealPlanRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMealPlan(userId, planId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getLimits_computesValues() {
        when(mealPlanRepository.countByUserId(userId)).thenReturn(3L);
        when(mealPlanRepository.countByUserIdAndStatus(userId, MealPlanStatus.ACTIVE)).thenReturn(1L);

        MealPlanLimitsDto limits = service.getLimits(userId);

        assertThat(limits.getMaxSavedMealPlans()).isEqualTo(10);
        assertThat(limits.getSavedMealPlans()).isEqualTo(3);
        assertThat(limits.getActiveMealPlans()).isEqualTo(1);
        assertThat(limits.getCanSaveMealPlan()).isTrue();
    }
}
