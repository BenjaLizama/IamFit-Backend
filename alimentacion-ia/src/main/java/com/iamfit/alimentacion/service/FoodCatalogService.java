package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.FoodItemDto;
import com.iamfit.alimentacion.dto.FoodSearchResponse;
import com.iamfit.alimentacion.entity.FoodItem;
import com.iamfit.alimentacion.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodCatalogService {

    private final FoodItemRepository foodItemRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${iamfit.calorieninjas.api-key:}")
    private String calorieNinjasApiKey;

    @Value("${iamfit.calorieninjas.base-url:https://api.calorieninjas.com/v1}")
    private String calorieNinjasBaseUrl;

    private static final int MIN_LOCAL_RESULTS = 3;

    // ─── Búsqueda combinada ───────────────────────────────────────────

    public FoodSearchResponse search(String query) {
        List<FoodItemDto> localResults = searchLocal(query);
        List<FoodItemDto> externalResults = new ArrayList<>();
        boolean externalSearchPerformed = false;

        if (localResults.size() < MIN_LOCAL_RESULTS && isExternalApiConfigured()) {
            externalResults = searchExternal(query);
            externalSearchPerformed = true;
            saveNewExternalResults(externalResults);
        }

        return FoodSearchResponse.builder()
                .localResults(localResults)
                .externalResults(externalResults)
                .externalSearchPerformed(externalSearchPerformed)
                .build();
    }

    // ─── Búsqueda local ──────────────────────────────────────────────

    public List<FoodItemDto> searchLocal(String query) {
        return foodItemRepository.searchByNameOrNameEn(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ─── Obtener por ID ──────────────────────────────────────────────

    public FoodItemDto getById(UUID id) {
        return foodItemRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Alimento no encontrado: " + id));
    }

    // ─── Obtener por categoría ───────────────────────────────────────

    public List<FoodItemDto> getByCategory(String category) {
        return foodItemRepository.findByFoodCategory(category)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ─── API externa — CalorieNinjas ─────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<FoodItemDto> searchExternal(String query) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(calorieNinjasBaseUrl + "/nutrition?query=" + query)
                    .header("X-Api-Key", calorieNinjasApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("items")) {
                return List.of();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            return items.stream()
                    .map(this::mapExternalItem)
                    .toList();

        } catch (Exception e) {
            log.error("Error consultando CalorieNinjas para query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    private FoodItemDto mapExternalItem(Map<String, Object> item) {
        return FoodItemDto.builder()
                .name(getString(item, "name"))
                .foodCategory("externo")
                .servingSizeG(getDouble(item, "serving_size_g"))
                .calories(getDouble(item, "calories"))
                .protein(getDouble(item, "protein_g"))
                .carbohydrates(getDouble(item, "carbohydrates_total_g"))
                .fat(getDouble(item, "fat_total_g"))
                .fiber(getDouble(item, "fiber_g"))
                .sugar(getDouble(item, "sugar_g"))
                .sodium(getDouble(item, "sodium_mg"))
                .build();
    }

    // ─── Guardar resultados externos en BD local ─────────────────────

    private void saveNewExternalResults(List<FoodItemDto> externalResults) {
        for (FoodItemDto dto : externalResults) {
            if (dto.getName() == null) continue;
            String externalId = "CN_" + dto.getName().toLowerCase().replace(" ", "_");
            if (!foodItemRepository.existsByExternalId(externalId)) {
                FoodItem item = new FoodItem();
                item.setName(dto.getName());
                item.setNameEn(dto.getName());
                item.setCalories(dto.getCalories() != null ? dto.getCalories() : 0.0);
                item.setProtein(dto.getProtein() != null ? dto.getProtein() : 0.0);
                item.setCarbohydrates(dto.getCarbohydrates() != null ? dto.getCarbohydrates() : 0.0);
                item.setFat(dto.getFat() != null ? dto.getFat() : 0.0);
                item.setFiber(dto.getFiber());
                item.setSugar(dto.getSugar());
                item.setSodium(dto.getSodium());
                item.setServingSizeG(dto.getServingSizeG() != null ? dto.getServingSizeG() : 100.0);
                item.setFoodCategory("externo");
                item.setExternalId(externalId);
                item.setIsVerified(false);
                foodItemRepository.save(item);
                log.info("Alimento externo guardado en catálogo local: {}", dto.getName());
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private boolean isExternalApiConfigured() {
        return calorieNinjasApiKey != null && !calorieNinjasApiKey.isBlank();
    }

    public FoodItemDto toDto(FoodItem item) {
        return FoodItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .foodCategory(item.getFoodCategory())
                .servingSizeG(item.getServingSizeG())
                .calories(item.getCalories())
                .protein(item.getProtein())
                .carbohydrates(item.getCarbohydrates())
                .fat(item.getFat())
                .fiber(item.getFiber())
                .sugar(item.getSugar())
                .sodium(item.getSodium())
                .build();
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Double d) return d;
        if (val instanceof Integer i) return i.doubleValue();
        if (val instanceof Float f) return f.doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return null; }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}