package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ProfileContextDTO {
    private UserProfileDTO profile;
    private Map<String, Object> routineLimits;
    private Map<String, Object> foodLimits;
    private Map<String, Object> activeMealPlan;
    private Map<String, Object> todayNutrition;
}