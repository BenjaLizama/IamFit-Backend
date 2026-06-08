package com.iamfit.ejercicios.service;

import com.iamfit.ejercicios.dto.ExerciseDto;
import com.iamfit.ejercicios.dto.ExerciseOptionsDto;
import com.iamfit.ejercicios.entity.Exercise;
import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseCatalogService {

    private final ExerciseRepository exerciseRepository;

    public List<ExerciseDto> getAll() {
        return exerciseRepository.findByIsActiveTrue()
                .stream().map(this::toDto).toList();
    }

    public List<ExerciseDto> getByMuscleGroup(MuscleGroup muscleGroup) {
        return exerciseRepository.findByMuscleGroupAndIsActiveTrue(muscleGroup)
                .stream().map(this::toDto).toList();
    }

    public List<ExerciseDto> search(String name) {
        return exerciseRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream().map(this::toDto).toList();
    }

    public List<ExerciseDto> filter(MuscleGroup muscleGroup,
                                    DifficultyLevel difficulty,
                                    Equipment equipment) {
        return exerciseRepository.findByFilters(muscleGroup, difficulty, equipment)
                .stream().map(this::toDto).toList();
    }

    public ExerciseDto getById(UUID id) {
        return exerciseRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + id));
    }

    public ExerciseDto toDto(Exercise exercise) {
        return ExerciseDto.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .muscleGroup(exercise.getMuscleGroup())
                .equipment(exercise.getEquipment())
                .difficulty(exercise.getDifficulty())
                .defaultSets(exercise.getDefaultSets())
                .defaultReps(exercise.getDefaultReps())
                .defaultRestSeconds(exercise.getDefaultRestSeconds())
                .videoUrl(exercise.getVideoUrl())
                .build();
    }

    public ExerciseOptionsDto getOptions() {
        return ExerciseOptionsDto.builder()
                .muscleGroups(List.of(Exercise.MuscleGroup.values()))
                .equipment(List.of(Exercise.Equipment.values()))
                .difficultyLevels(List.of(Exercise.DifficultyLevel.values()))
                .build();
    }
}