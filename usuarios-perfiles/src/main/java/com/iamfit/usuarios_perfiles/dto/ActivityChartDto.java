package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ActivityChartDto {
    private String type;
    private String period;
    private String unit;
    private String label;
    private List<ActivityPointDto> points;
    private ActivityPointDto highlight;
}