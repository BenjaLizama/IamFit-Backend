package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityPointDto {
    private String label;
    private Double value;
}