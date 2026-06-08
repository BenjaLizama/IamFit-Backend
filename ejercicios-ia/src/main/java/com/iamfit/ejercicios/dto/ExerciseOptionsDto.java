package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ExerciseOptionsDto {
    private List<MuscleGroup> muscleGroups;
    private List<Equipment> equipment;
    private List<DifficultyLevel> difficultyLevels;
}