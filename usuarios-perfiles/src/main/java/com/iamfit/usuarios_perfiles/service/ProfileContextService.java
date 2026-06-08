package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class ProfileContextService {

    private final WebClient alimentacionClient;
    private final WebClient ejerciciosClient;

    public ProfileContextService(
            WebClient.Builder webClientBuilder,
            @Value("${iamfit.services.alimentacion.base-url:http://alimentacion-app-local:8080}") String alimentacionUrl,
            @Value("${iamfit.services.ejercicios.base-url:http://ejercicios-app-local:8080}") String ejerciciosUrl) {
        this.alimentacionClient = webClientBuilder.baseUrl(alimentacionUrl).build();
        this.ejerciciosClient = webClientBuilder.baseUrl(ejerciciosUrl).build();
    }

    public ProfileContextDTO buildContext(UserProfileDTO profile, String token) {
        Map<String, Object> routineLimits = fetchRoutineLimits(token);
        Map<String, Object> foodLimits = fetchFoodLimits(token);
        Map<String, Object> activeMealPlan = fetchActiveMealPlan(token);
        Map<String, Object> todayNutrition = fetchTodayNutrition(token);

        return ProfileContextDTO.builder()
                .profile(profile)
                .routineLimits(routineLimits)
                .foodLimits(foodLimits)
                .activeMealPlan(activeMealPlan)
                .todayNutrition(todayNutrition)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRoutineLimits(String token) {
        try {
            return ejerciciosClient.get()
                    .uri("/api/v1/routines/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo obtener limites de rutinas: {}", e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchFoodLimits(String token) {
        try {
            return alimentacionClient.get()
                    .uri("/api/v1/food/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo obtener limites de alimentos: {}", e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchActiveMealPlan(String token) {
        try {
            return alimentacionClient.get()
                    .uri("/api/v1/food/meal-plans/active")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.debug("No hay plan activo: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchTodayNutrition(String token) {
        try {
            return alimentacionClient.get()
                    .uri("/api/v1/food/calories")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo obtener nutricion de hoy: {}", e.getMessage());
            return Map.of();
        }
    }
}