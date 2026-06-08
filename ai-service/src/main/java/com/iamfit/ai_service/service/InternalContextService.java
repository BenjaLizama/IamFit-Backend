package com.iamfit.ai_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class InternalContextService {

    private final WebClient alimentacionClient;
    private final WebClient ejerciciosClient;

    public InternalContextService(
            WebClient.Builder webClientBuilder,
            @Value("${iamfit.services.alimentacion.base-url}") String alimentacionUrl,
            @Value("${iamfit.services.ejercicios.base-url}") String ejerciciosUrl) {
        this.alimentacionClient = webClientBuilder
                .baseUrl(alimentacionUrl)
                .build();
        this.ejerciciosClient = webClientBuilder
                .baseUrl(ejerciciosUrl)
                .build();
    }

    // ─── Resumen nutricional del dia ─────────────────────────────────

    public String getNutritionContext(String token) {
        try {
            Map response = alimentacionClient.get()
                    .uri("/api/v1/food/calories")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "";

            return String.format("""
                [CONTEXTO NUTRICIONAL DE HOY]
                - Calorias totales: %.0f kcal
                - Proteinas: %.1f g
                - Carbohidratos: %.1f g
                - Grasas: %.1f g
                - Fibra: %.1f g
                [FIN CONTEXTO NUTRICIONAL]
                """,
                    getDouble(response, "totalCalories"),
                    getDouble(response, "totalProtein"),
                    getDouble(response, "totalCarbohydrates"),
                    getDouble(response, "totalFat"),
                    getDouble(response, "totalFiber"));

        } catch (Exception e) {
            log.warn("No se pudo obtener contexto nutricional: {}", e.getMessage());
            return "";
        }
    }

    // ─── Rutinas activas ─────────────────────────────────────────────

    public String getRoutinesContext(String token) {
        try {
            Object[] routines = ejerciciosClient.get()
                    .uri("/api/v1/routines?status=ACTIVE")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Object[].class)
                    .block();

            if (routines == null || routines.length == 0) {
                return "[RUTINAS] El usuario no tiene rutinas activas.\n";
            }

            StringBuilder sb = new StringBuilder("[RUTINAS ACTIVAS DEL USUARIO]\n");
            for (Object r : routines) {
                if (r instanceof Map<?, ?> routine) {
                    sb.append(String.format("- %s (dificultad: %s, duracion: %s min)\n",
                            routine.get("name"),
                            routine.get("difficultyLevel"),
                            routine.get("estimatedDurationMinutes")));
                }
            }
            sb.append("[FIN RUTINAS]\n");
            return sb.toString();

        } catch (Exception e) {
            log.warn("No se pudo obtener contexto de rutinas: {}", e.getMessage());
            return "";
        }
    }

    // ─── Limites de rutinas ──────────────────────────────────────────

    public String getRoutineLimitsContext(String token) {
        try {
            Map response = ejerciciosClient.get()
                    .uri("/api/v1/routines/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "";

            boolean canCreate = Boolean.TRUE.equals(response.get("canCreateRoutine"));
            return String.format("[LIMITES DE RUTINAS] Activas: %s/%s. Puede crear: %s\n",
                    response.get("activeRoutines"),
                    response.get("maxActiveRoutines"),
                    canCreate ? "Sí" : "No — debe desactivar una primero");

        } catch (Exception e) {
            log.warn("No se pudo obtener limites de rutinas: {}", e.getMessage());
            return "";
        }
    }

    // ─── Plan de comidas activo ──────────────────────────────────────

    public String getActiveMealPlanContext(String token) {
        try {
            Map response = alimentacionClient.get()
                    .uri("/api/v1/food/meal-plans/active")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "";

            return String.format("[PLAN DE COMIDAS ACTIVO] Titulo: %s, Objetivo: %s\n",
                    response.get("title"), response.get("goal"));

        } catch (Exception e) {
            // 404 es esperado si no hay plan activo — no es un error real
            log.debug("No hay plan de comidas activo: {}", e.getMessage());
            return "[PLAN DE COMIDAS] El usuario no tiene un plan activo.\n";
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────

    private double getDouble(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        if (val instanceof Double d) return d;
        if (val instanceof Integer i) return i.doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }
}