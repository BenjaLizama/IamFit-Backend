package com.iamfit.usuarios_perfiles.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileSummaryDto {
    private Long workoutsCount;
    private Long foodEntriesCount;
    private Integer activeRoutinesCount;
    private Integer activeMealPlansCount;
    private Integer currentStreakDays;
}