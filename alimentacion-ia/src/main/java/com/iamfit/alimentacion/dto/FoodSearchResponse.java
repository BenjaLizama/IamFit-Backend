package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FoodSearchResponse {
    private List<FoodItemDto> localResults;
    private List<FoodItemDto> externalResults;
    private boolean externalSearchPerformed;
}