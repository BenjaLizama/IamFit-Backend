package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ActiveItemsDto {
    private List<Map<String, Object>> activeRoutines;
    private Map<String, Object> activeMealPlan;
}