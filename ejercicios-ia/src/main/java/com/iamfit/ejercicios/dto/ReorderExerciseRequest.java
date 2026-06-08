package com.iamfit.ejercicios.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ReorderExerciseRequest {
    @NotNull
    private UUID exerciseEntryId;
    @NotNull
    private Integer newOrderIndex;
}