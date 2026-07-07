package com.iamfit.ejercicios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ejercicios.dto.*;
import com.iamfit.ejercicios.entity.Exercise;
import com.iamfit.ejercicios.entity.Routine;
import com.iamfit.ejercicios.entity.RoutineExercise;
import com.iamfit.ejercicios.exception.RoutineLimitReachedException;
import com.iamfit.ejercicios.exception.RoutineSessionExpiredException;
import com.iamfit.ejercicios.repository.ExerciseRepository;
import com.iamfit.ejercicios.repository.RoutineExerciseRepository;
import com.iamfit.ejercicios.repository.RoutineRepository;
import com.iamfit.ejercicios.repository.WorkoutHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    @Mock private RoutineRepository routineRepository;
    @Mock private RoutineExerciseRepository routineExerciseRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private WorkoutHistoryRepository workoutHistoryRepository;
    @Mock private ExerciseCatalogService exerciseCatalogService;
    @Mock private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    // Usamos un ObjectMapper real para testear el parseo del JSON sin volvernos locos mockeándolo
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoutineService routineService;

    private final String userId = "user-123";
    private final UUID routineId = UUID.randomUUID();
    private Routine mockRoutine;

    @BeforeEach
    void setUp() {
        mockRoutine = new Routine();
        mockRoutine.setId(routineId);
        mockRoutine.setUserId(userId);
        mockRoutine.setName("Rutina Mock");
        mockRoutine.setIsActive(true);
        mockRoutine.setExercises(new ArrayList<>());
    }

    // ==========================================
    // TESTS: Generación con IA
    // ==========================================

    @Test
    @DisplayName("generateRoutines: Debería llamar a Vertex AI, parsear el JSON y guardar en sesión")
    void generateRoutines_Success() {
        // GIVEN
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);

        GenerateRoutineRequest request = new GenerateRoutineRequest();
        request.setDifficulty(Exercise.DifficultyLevel.PRINCIPIANTE);
        request.setMuscleGroups(List.of(Exercise.MuscleGroup.PECHO));

        // Simulamos la respuesta de la IA en formato JSON válido
        String iaJsonResponse = """
        {
          "routines": [
            {
              "name": "Pecho Principiante",
              "description": "Rutina básica",
              "estimatedDurationMinutes": 45,
              "exercises": [
                {
                  "exerciseName": "Flexiones",
                  "muscleGroup": "CHEST",
                  "sets": 3,
                  "reps": 10,
                  "restSeconds": 60
                }
              ]
            }
          ]
        }
        """;

        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(iaJsonResponse);

        // WHEN
        GeneratedRoutinesResponse response = routineService.generateRoutines(userId, request);

        // THEN
        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertEquals(1, response.getRoutines().size());
        assertEquals("Pecho Principiante", response.getRoutines().get(0).getName());

        // Verificar que se guardó en el mapa en memoria (vía Reflection)
        Map<String, List<RoutineDto>> sessions = (Map<String, List<RoutineDto>>)
                ReflectionTestUtils.getField(routineService, "generatedSessions");
        assertTrue(sessions.containsKey(response.getSessionId()));
    }

    // ==========================================
    // TESTS: Selección de Rutina
    // ==========================================

    @Test
    @DisplayName("selectRoutine: Debería fallar si la sesión expiró o no existe")
    void selectRoutine_SessionExpired() {
        SelectRoutineRequest request = new SelectRoutineRequest();
        request.setSessionId("invalid-session");
        request.setSelectedIndex(0);

        assertThrows(RoutineSessionExpiredException.class, () -> {
            routineService.selectRoutine(userId, request);
        });
    }

    @Test
    @DisplayName("selectRoutine: Debería fallar si alcanzó el límite máximo de rutinas (5)")
    void selectRoutine_LimitReached() {
        // GIVEN
        String sessionId = "session-test";
        RoutineDto dto = RoutineDto.builder().name("Rutina").exercises(List.of()).build();

        // Inyectamos la sesión artificialmente para aislar el test
        Map<String, List<RoutineDto>> sessions = new ConcurrentHashMap<>();
        sessions.put(sessionId, List.of(dto));
        ReflectionTestUtils.setField(routineService, "generatedSessions", sessions);

        SelectRoutineRequest request = new SelectRoutineRequest();
        request.setSessionId(sessionId);
        request.setSelectedIndex(0);

        when(routineRepository.countByUserIdAndIsActiveTrue(userId)).thenReturn(5L); // Límite alcanzado

        // WHEN / THEN
        assertThrows(RoutineLimitReachedException.class, () -> {
            routineService.selectRoutine(userId, request);
        });
    }

    @Test
    @DisplayName("selectRoutine: Debería guardar la rutina seleccionada correctamente")
    void selectRoutine_Success() {
        // GIVEN
        String sessionId = "session-test";
        RoutineExerciseDto exDto = RoutineExerciseDto.builder().exerciseName("Sentadilla").build();
        RoutineDto dto = RoutineDto.builder()
                .name("Piernas")
                .exercises(List.of(exDto))
                .build();

        Map<String, List<RoutineDto>> sessions = new ConcurrentHashMap<>();
        sessions.put(sessionId, List.of(dto));
        ReflectionTestUtils.setField(routineService, "generatedSessions", sessions);

        SelectRoutineRequest request = new SelectRoutineRequest();
        request.setSessionId(sessionId);
        request.setSelectedIndex(0);

        // ─── SOLUCIÓN: Mockear el ejercicio para que no devuelva null ───
        Exercise mockExercise = new Exercise();
        mockExercise.setId(UUID.randomUUID()); // Es vital que tenga ID ya que toExerciseDto() lo mapea
        mockExercise.setName("Sentadilla");
        mockExercise.setMuscleGroup(Exercise.MuscleGroup.CUERPO_COMPLETO);

        // Simulamos que el catálogo encuentra el ejercicio correctamente
        when(exerciseRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(anyString()))
                .thenReturn(List.of(mockExercise));
        // ───────────────────────────────────────────────────────────────

        when(routineRepository.countByUserIdAndIsActiveTrue(userId)).thenReturn(2L);
        when(routineRepository.save(any(Routine.class))).thenReturn(mockRoutine);

        // WHEN
        RoutineDto result = routineService.selectRoutine(userId, request);

        // THEN
        assertNotNull(result);
        verify(routineRepository, times(1)).save(any(Routine.class));
        verify(routineExerciseRepository, times(1)).saveAll(anyList());

        // La sesión debe ser eliminada después de usarse
        assertFalse(sessions.containsKey(sessionId));
    }
    // ==========================================
    // TESTS: Operaciones CRUD y Lógica de Negocio
    // ==========================================

    @Test
    @DisplayName("getRoutineLimits: Debería calcular correctamente los límites")
    void getRoutineLimits_Success() {
        // GIVEN
        when(routineRepository.countByUserIdAndIsActiveTrue(userId)).thenReturn(3L);
        when(routineRepository.countByUserIdAndIsActiveFalse(userId)).thenReturn(2L);

        // WHEN
        RoutineLimitsDto limits = routineService.getRoutineLimits(userId);

        // THEN
        assertEquals(5, limits.getMaxActiveRoutines());
        assertEquals(3L, limits.getActiveRoutines());
        assertEquals(2L, limits.getInactiveRoutines());
        assertTrue(limits.getCanCreateRoutine()); // 3 < 5
    }

    @Test
    @DisplayName("addExerciseToRoutine: Debería agregar el ejercicio y guardarlo en la base de datos")
    void addExerciseToRoutine_Success() {
        // GIVEN
        UUID exId = UUID.randomUUID();
        Exercise mockExercise = new Exercise();
        mockExercise.setId(exId);
        mockExercise.setName("Press");

        AddExerciseToRoutineRequest request = new AddExerciseToRoutineRequest();
        request.setExerciseId(exId);
        request.setSets(4);
        request.setReps(10);

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(mockRoutine));
        when(exerciseRepository.findById(exId)).thenReturn(Optional.of(mockExercise));

        // WHEN
        RoutineDto result = routineService.addExerciseToRoutine(userId, routineId, request);

        // THEN
        assertNotNull(result);
        assertEquals(1, mockRoutine.getExercises().size()); // Se agregó a la lista
        verify(routineExerciseRepository, times(1)).save(any(RoutineExercise.class));
    }

    @Test
    @DisplayName("deleteRoutine: Debería marcar isActive en false en lugar de borrar el registro (Borrado Lógico)")
    void deleteRoutine_LogicalDelete() {
        // GIVEN
        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(mockRoutine));

        // WHEN
        routineService.deleteRoutine(userId, routineId);

        // THEN
        assertFalse(mockRoutine.getIsActive());
        verify(routineRepository, times(1)).save(mockRoutine);
    }
}