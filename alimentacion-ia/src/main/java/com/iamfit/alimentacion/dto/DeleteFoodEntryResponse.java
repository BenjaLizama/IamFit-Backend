package com.iamfit.alimentacion.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class DeleteFoodEntryResponse {
    private UUID deletedId;
    private String message;
}