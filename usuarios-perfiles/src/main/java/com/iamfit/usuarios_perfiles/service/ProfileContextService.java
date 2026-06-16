package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.*;
import com.iamfit.usuarios_perfiles.entity.WeightHistoryEntity;
import com.iamfit.usuarios_perfiles.repository.WeightHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ProfileContextService {

    private final WebClient alimentacionClient;
    private final WebClient ejerciciosClient;
    private final WeightHistoryRepository weightHistoryRepository;

    public ProfileContextService(
            WebClient.Builder webClientBuilder,
            @Value("${iamfit.services.alimentacion.base-url:http://alimentacion-app-local:8080}") String alimentacionUrl,
            @Value("${iamfit.services.ejercicios.base-url:http://ejercicios-app-local:8080}") String ejerciciosUrl,
            WeightHistoryRepository weightHistoryRepository) {
        this.alimentacionClient = webClientBuilder.baseUrl(alimentacionUrl).build();
        this.ejerciciosClient = webClientBuilder.baseUrl(ejerciciosUrl).build();
        this.weightHistoryRepository = weightHistoryRepository;
    }

    // ─── Context ─────────────────────────────────────────────────────

    public ProfileContextDTO buildContext(UserProfileDTO profile, String token) {
        return ProfileContextDTO.builder()
                .profile(profile)
                .routineLimits(fetchRoutineLimits(token))
                .foodLimits(fetchFoodLimits(token))
                .activeMealPlan(fetchActiveMealPlan(token))
                .todayNutrition(fetchTodayNutrition(token))
                .build();
    }

    // ─── Summary ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public ProfileSummaryDto buildSummary(String token) {
        int activeRoutines = 0;
        int activeMealPlans = 0;
        long workoutsCount = 0;
        long foodEntriesCount = 0;

        try {
            Map routineLimits = ejerciciosClient.get()
                    .uri("/api/v1/routines/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(Map.class).block();
            if (routineLimits != null) {
                Object ar = routineLimits.get("activeRoutines");
                if (ar instanceof Number n) activeRoutines = n.intValue();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener routine limits: {}", e.getMessage());
        }

        try {
            Map mealLimits = alimentacionClient.get()
                    .uri("/api/v1/food/meal-plans/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(Map.class).block();
            if (mealLimits != null) {
                Object am = mealLimits.get("activeMealPlans");
                if (am instanceof Number n) activeMealPlans = n.intValue();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener meal plan limits: {}", e.getMessage());
        }

        try {
            List history = ejerciciosClient.get()
                    .uri("/api/v1/routines/history")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();
            if (history != null) workoutsCount = history.size();
        } catch (Exception e) {
            log.warn("No se pudo obtener workout history: {}", e.getMessage());
        }

        try {
            Map nutrition = alimentacionClient.get()
                    .uri("/api/v1/food/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(Map.class).block();
            if (nutrition != null) {
                Object ef = nutrition.get("entriesForDate");
                if (ef instanceof Number n) foodEntriesCount = n.longValue();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener food limits: {}", e.getMessage());
        }

        return ProfileSummaryDto.builder()
                .workoutsCount(workoutsCount)
                .foodEntriesCount(foodEntriesCount)
                .activeRoutinesCount(activeRoutines)
                .activeMealPlansCount(activeMealPlans)
                .currentStreakDays(0)
                .build();
    }

    // ─── Activity chart ──────────────────────────────────────────────

    public ActivityChartDto buildActivityChart(String token, String userId,
                                               String type, String period) {
        String normalizedType = type != null ? type.toUpperCase() : "WORKOUTS";
        String normalizedPeriod = period != null ? period.toUpperCase() : "MONTHLY";

        return switch (normalizedType) {
            case "WORKOUTS" -> buildWorkoutsChart(token, normalizedPeriod);
            case "WEIGHT" -> buildWeightChart(userId, normalizedPeriod);
            case "CALORIES" -> buildCaloriesChart(token, normalizedPeriod);
            case "PROTEIN" -> buildProteinChart(token, normalizedPeriod);
            case "ROUTINES" -> buildRoutinesChart(token, normalizedPeriod);
            default -> buildWorkoutsChart(token, normalizedPeriod);
        };
    }

    @SuppressWarnings("unchecked")
    private ActivityChartDto buildWorkoutsChart(String token, String period) {
        java.util.List<ActivityPointDto> points = new java.util.ArrayList<>();
        ActivityPointDto highlight = null;

        String uri = switch (period) {
            case "DAILY" -> "/api/v1/routines/history/daily";
            case "WEEKLY" -> "/api/v1/routines/history/weekly";
            default -> "/api/v1/routines/history/monthly";
        };

        String labelKey = period.equals("DAILY") ? "date" :
                period.equals("WEEKLY") ? "week" : "month";
        String valueKey = "count";

        try {
            List data = ejerciciosClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();

            if (data != null) {
                for (Object item : data) {
                    if (item instanceof Map<?, ?> entry) {
                        String label = formatLabel(String.valueOf(entry.get(labelKey)), period);
                        Object val = entry.get(valueKey);
                        if (label != null && val instanceof Number n) {
                            ActivityPointDto point = ActivityPointDto.builder()
                                    .label(label).value(n.doubleValue()).build();
                            points.add(point);
                            if (highlight == null || n.doubleValue() > highlight.getValue())
                                highlight = point;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener workouts chart: {}", e.getMessage());
        }

        return ActivityChartDto.builder()
                .type("WORKOUTS").period(period)
                .unit("entrenamientos").label("Entrenamientos completados")
                .points(points).highlight(highlight).build();
    }

    @SuppressWarnings("unchecked")
    private ActivityChartDto buildCaloriesChart(String token, String period) {
        String uri = switch (period) {
            case "DAILY" -> "/api/v1/food/calories/daily";
            case "WEEKLY" -> "/api/v1/food/calories/weekly";
            default -> "/api/v1/food/calories/monthly";
        };
        String labelKey = period.equals("DAILY") ? "date" :
                period.equals("WEEKLY") ? "week" : "month";

        return buildNutritionChart(token, period, uri, labelKey, "totalCalories",
                "CALORIES", "kcal", "Calorias consumidas");
    }



    @SuppressWarnings("unchecked")
    private ActivityChartDto buildRoutinesChart(String token, String period) {
        String uri = switch (period) {
            case "DAILY" -> "/api/v1/routines/history/daily";
            case "WEEKLY" -> "/api/v1/routines/history/weekly";
            default -> "/api/v1/routines/history/monthly";
        };
        String labelKey = period.equals("DAILY") ? "date" :
                period.equals("WEEKLY") ? "week" : "month";

        java.util.List<ActivityPointDto> points = new java.util.ArrayList<>();
        ActivityPointDto highlight = null;

        try {
            List data = ejerciciosClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();

            if (data != null) {
                for (Object item : data) {
                    if (item instanceof Map<?, ?> entry) {
                        String label = formatLabel(String.valueOf(entry.get(labelKey)), period);
                        Object val = entry.get("count");
                        if (label != null && val instanceof Number n) {
                            ActivityPointDto point = ActivityPointDto.builder()
                                    .label(label).value(n.doubleValue()).build();
                            points.add(point);
                            if (highlight == null || n.doubleValue() > highlight.getValue())
                                highlight = point;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener routines chart: {}", e.getMessage());
        }

        return ActivityChartDto.builder()
                .type("ROUTINES").period(period)
                .unit("rutinas").label("Rutinas completadas")
                .points(points).highlight(highlight).build();
    }

    @SuppressWarnings("unchecked")
    private ActivityChartDto buildNutritionChart(String token, String period,
                                                 String uri, String labelKey, String valueKey,
                                                 String type, String unit, String label) {
        java.util.List<ActivityPointDto> points = new java.util.ArrayList<>();
        ActivityPointDto highlight = null;

        try {
            List data = alimentacionClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();

            if (data != null) {
                for (Object item : data) {
                    if (item instanceof Map<?, ?> entry) {
                        String lbl = formatLabel(String.valueOf(entry.get(labelKey)), period);
                        Object val = entry.get(valueKey);
                        if (lbl != null && val instanceof Number n) {
                            ActivityPointDto point = ActivityPointDto.builder()
                                    .label(lbl).value(n.doubleValue()).build();
                            points.add(point);
                            if (highlight == null || n.doubleValue() > highlight.getValue())
                                highlight = point;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener {} chart: {}", type, e.getMessage());
        }

        return ActivityChartDto.builder()
                .type(type).period(period)
                .unit(unit).label(label + " por " + period.toLowerCase())
                .points(points).highlight(highlight).build();
    }

    // Formatea el label según el período
    private String formatLabel(String raw, String period) {
        if (raw == null || raw.equals("null")) return null;
        return switch (period) {
            case "DAILY" -> raw.length() >= 10 ?
                    raw.substring(8, 10) + "/" + raw.substring(5, 7) : raw; // DD/MM
            case "WEEKLY" -> raw; // W23 ya viene formateado
            default -> raw.length() >= 7 ? raw.substring(5, 7) : raw; // MM
        };
    }

    private ActivityChartDto buildWeightChart(String userId, String period) {
        try {
            UUID credentialId = UUID.fromString(userId);
            List<WeightHistoryEntity> history = weightHistoryRepository
                    .findByCredentialIdOrderByCreatedAt(credentialId);

            if (history.isEmpty()) {
                return ActivityChartDto.builder()
                        .type("WEIGHT").period(period)
                        .unit("kg").label("Historial de peso")
                        .points(List.of()).highlight(null)
                        .build();
            }

            java.util.Map<String, Double> weightByMonth = new java.util.LinkedHashMap<>();
            for (WeightHistoryEntity entry : history) {
                if (entry.getCreatedAt() != null) {
                    String month = entry.getCreatedAt().toString().substring(0, 7);
                    weightByMonth.put(month, entry.getWeight().doubleValue());
                }
            }

            java.util.List<ActivityPointDto> points = new java.util.ArrayList<>();
            ActivityPointDto highlight = null;

            for (var e : weightByMonth.entrySet()) {
                ActivityPointDto point = ActivityPointDto.builder()
                        .label(e.getKey().substring(5))
                        .value(e.getValue())
                        .build();
                points.add(point);
                if (highlight == null || e.getValue() < highlight.getValue()) {
                    highlight = point;
                }
            }

            return ActivityChartDto.builder()
                    .type("WEIGHT").period(period)
                    .unit("kg").label("Historial de peso")
                    .points(points).highlight(highlight)
                    .build();

        } catch (Exception e) {
            log.warn("No se pudo construir chart de peso: {}", e.getMessage());
            return ActivityChartDto.builder()
                    .type("WEIGHT").period(period)
                    .unit("kg").label("Historial de peso")
                    .points(List.of()).highlight(null)
                    .build();
        }
    }



    // ─── Active items ────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public ActiveItemsDto buildActiveItems(String token) {
        java.util.List<Map<String, Object>> activeRoutines = new java.util.ArrayList<>();
        Map<String, Object> activeMealPlan = null;

        try {
            List routines = ejerciciosClient.get()
                    .uri("/api/v1/routines?status=ACTIVE")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();
            if (routines != null) {
                for (Object r : routines) {
                    if (r instanceof Map<?, ?> routine) {
                        activeRoutines.add(Map.of(
                                "id", String.valueOf(routine.get("id")),
                                "name", String.valueOf(routine.get("name")),
                                "durationMinutes", routine.getOrDefault("estimatedDurationMinutes", null)
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener rutinas activas: {}", e.getMessage());
        }

        try {
            Map plan = alimentacionClient.get()
                    .uri("/api/v1/food/meal-plans/active")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(Map.class).block();
            if (plan != null) {
                activeMealPlan = Map.of(
                        "id", String.valueOf(plan.get("id")),
                        "name", String.valueOf(plan.getOrDefault("title", "")),
                        "goal", String.valueOf(plan.getOrDefault("goal", ""))
                );
            }
        } catch (Exception e) {
            log.debug("No hay plan activo: {}", e.getMessage());
        }

        return ActiveItemsDto.builder()
                .activeRoutines(activeRoutines)
                .activeMealPlan(activeMealPlan)
                .build();
    }

    // ─── Helpers privados ────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRoutineLimits(String token) {
        try {
            return ejerciciosClient.get()
                    .uri("/api/v1/routines/limits")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(Map.class).block();
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
                    .retrieve().bodyToMono(Map.class).block();
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
                    .retrieve().bodyToMono(Map.class).block();
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
                    .retrieve().bodyToMono(Map.class).block();
        } catch (Exception e) {
            log.warn("No se pudo obtener nutricion de hoy: {}", e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private ActivityChartDto buildProteinChart(String token, String period) {
        java.util.List<ActivityPointDto> points = new java.util.ArrayList<>();
        ActivityPointDto highlight = null;

        try {
            List data = alimentacionClient.get()
                    .uri("/api/v1/food/protein/monthly")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().bodyToMono(List.class).block();

            if (data != null) {
                for (Object item : data) {
                    if (item instanceof Map<?, ?> entry) {
                        String month = (String) entry.get("month");
                        Object protein = entry.get("totalProtein");
                        if (month != null && protein instanceof Number n) {
                            ActivityPointDto point = ActivityPointDto.builder()
                                    .label(month.substring(5))
                                    .value(n.doubleValue())
                                    .build();
                            points.add(point);
                            if (highlight == null || n.doubleValue() > highlight.getValue()) {
                                highlight = point;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener proteinas mensuales: {}", e.getMessage());
        }

        return ActivityChartDto.builder()
                .type("PROTEIN").period(period)
                .unit("g").label("Proteinas consumidas por mes")
                .points(points).highlight(highlight)
                .build();
    }



}