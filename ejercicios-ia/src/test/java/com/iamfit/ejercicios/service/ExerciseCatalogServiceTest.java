package com.iamfit.ejercicios.service;

import com.iamfit.ejercicios.dto.ExerciseDto;
import com.iamfit.ejercicios.dto.ExerciseOptionsDto;
import com.iamfit.ejercicios.entity.Exercise;
import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseCatalogServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseCatalogService exerciseCatalogService;

    private Exercise mockExercise;
    private final UUID exerciseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Inicializamos un Exercise simulado para reutilizarlo en los tests
        mockExercise = new Exercise();
        mockExercise.setId(exerciseId);
        mockExercise.setName("Flexiones de pecho");
        mockExercise.setDescription("Ejercicio básico de empuje");
        mockExercise.setMuscleGroup(MuscleGroup.PECHO);
        mockExercise.setEquipment(Equipment.PESO_CORPORAL);
        mockExercise.setDifficulty(DifficultyLevel.PRINCIPIANTE);
        mockExercise.setDefaultSets(3);
        mockExercise.setDefaultReps(12);
        mockExercise.setDefaultRestSeconds(60);
        mockExercise.setVideoUrl("http://video.com/flexiones");
    }

    @Test
    @DisplayName("getAll: Debería retornar lista de DTOs de ejercicios activos")
    void getAll_Success() {
        // GIVEN
        when(exerciseRepository.findByIsActiveTrue()).thenReturn(List.of(mockExercise));

        // WHEN
        List<ExerciseDto> result = exerciseCatalogService.getAll();

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Flexiones de pecho", result.get(0).getName());
        verify(exerciseRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getByMuscleGroup: Debería retornar ejercicios filtrados por grupo muscular")
    void getByMuscleGroup_Success() {
        // GIVEN
        when(exerciseRepository.findByMuscleGroupAndIsActiveTrue(MuscleGroup.PECHO))
                .thenReturn(List.of(mockExercise));

        // WHEN
        List<ExerciseDto> result = exerciseCatalogService.getByMuscleGroup(MuscleGroup.PECHO);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MuscleGroup.PECHO, result.get(0).getMuscleGroup());
        verify(exerciseRepository, times(1)).findByMuscleGroupAndIsActiveTrue(MuscleGroup.PECHO);
    }

    @Test
    @DisplayName("search: Debería retornar ejercicios filtrados por nombre")
    void search_Success() {
        // GIVEN
        when(exerciseRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("Flexiones"))
                .thenReturn(List.of(mockExercise));

        // WHEN
        List<ExerciseDto> result = exerciseCatalogService.search("Flexiones");

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Flexiones de pecho", result.get(0).getName());
        verify(exerciseRepository, times(1)).findByNameContainingIgnoreCaseAndIsActiveTrue("Flexiones");
    }

    @Test
    @DisplayName("filter: Debería retornar ejercicios según múltiples filtros")
    void filter_Success() {
        // GIVEN
        when(exerciseRepository.findByFilters(MuscleGroup.PECHO, DifficultyLevel.PRINCIPIANTE, Equipment.PESO_CORPORAL))
                .thenReturn(List.of(mockExercise));

        // WHEN
        List<ExerciseDto> result = exerciseCatalogService.filter(
                MuscleGroup.PECHO, DifficultyLevel.PRINCIPIANTE, Equipment.PESO_CORPORAL);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exerciseRepository, times(1))
                .findByFilters(MuscleGroup.PECHO, DifficultyLevel.PRINCIPIANTE, Equipment.PESO_CORPORAL);
    }

    @Test
    @DisplayName("getById: Debería retornar el DTO si el ejercicio existe")
    void getById_Success() {
        // GIVEN
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(mockExercise));

        // WHEN
        ExerciseDto result = exerciseCatalogService.getById(exerciseId);

        // THEN
        assertNotNull(result);
        assertEquals(exerciseId, result.getId());
        assertEquals("Flexiones de pecho", result.getName());
        verify(exerciseRepository, times(1)).findById(exerciseId);
    }

    @Test
    @DisplayName("getById: Debería lanzar RuntimeException si el ejercicio no existe")
    void getById_ThrowsException() {
        // GIVEN
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        // WHEN / THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exerciseCatalogService.getById(exerciseId);
        });

        assertEquals("Ejercicio no encontrado: " + exerciseId, exception.getMessage());
        verify(exerciseRepository, times(1)).findById(exerciseId);
    }

    @Test
    @DisplayName("toDto: Debería mapear correctamente de Entity a DTO")
    void toDto_MapsCorrectly() {
        // WHEN
        ExerciseDto dto = exerciseCatalogService.toDto(mockExercise);

        // THEN
        assertNotNull(dto);
        assertEquals(mockExercise.getId(), dto.getId());
        assertEquals(mockExercise.getName(), dto.getName());
        assertEquals(mockExercise.getDescription(), dto.getDescription());
        assertEquals(mockExercise.getMuscleGroup(), dto.getMuscleGroup());
        assertEquals(mockExercise.getEquipment(), dto.getEquipment());
        assertEquals(mockExercise.getDifficulty(), dto.getDifficulty());
        assertEquals(mockExercise.getDefaultSets(), dto.getDefaultSets());
        assertEquals(mockExercise.getDefaultReps(), dto.getDefaultReps());
        assertEquals(mockExercise.getDefaultRestSeconds(), dto.getDefaultRestSeconds());
        assertEquals(mockExercise.getVideoUrl(), dto.getVideoUrl());
    }

    @Test
    @DisplayName("getOptions: Debería retornar todos los valores de los enums")
    void getOptions_ReturnsAllEnumValues() {
        // WHEN
        ExerciseOptionsDto options = exerciseCatalogService.getOptions();

        // THEN
        assertNotNull(options);

        assertNotNull(options.getMuscleGroups());
        assertFalse(options.getMuscleGroups().isEmpty());
        assertEquals(MuscleGroup.values().length, options.getMuscleGroups().size());

        assertNotNull(options.getEquipment());
        assertFalse(options.getEquipment().isEmpty());
        assertEquals(Equipment.values().length, options.getEquipment().size());

        assertNotNull(options.getDifficultyLevels());
        assertFalse(options.getDifficultyLevels().isEmpty());
        assertEquals(DifficultyLevel.values().length, options.getDifficultyLevels().size());
    }
}