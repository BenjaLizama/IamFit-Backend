package com.iamfit.ejercicios.dto;

import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GenerateRoutineRequest {

    @NotNull(message = "La dificultad es obligatoria")
    private DifficultyLevel difficulty;

    @NotNull(message = "Debe seleccionar al menos un grupo muscular")
    private List<MuscleGroup> muscleGroups;

    private List<Equipment> availableEquipment;

    private Integer durationMinutes; // 30, 45, 60, 75

    private String limitations; // lesiones o restricciones del usuario
}