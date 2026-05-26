package com.iamfit.ejercicios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ejercicios.dto.*;
import com.iamfit.ejercicios.entity.*;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutHistoryRepository workoutHistoryRepository;
    private final ExerciseCatalogService exerciseCatalogService;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final int MAX_ROUTINES = 5;

    // Sesiones temporales de rutinas generadas (en memoria)
    private final Map<String, List<RoutineDto>> generatedSessions = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            Eres un entrenador personal experto en fitness y diseño de rutinas de entrenamiento.
            Tu objetivo es generar rutinas de ejercicio personalizadas, seguras y efectivas.
            
            Reglas estrictas:
            1. Genera exactamente 3 rutinas diferentes para el mismo objetivo.
            2. Cada rutina debe tener entre 4 y 8 ejercicios.
            3. Respeta absolutamente las limitaciones físicas indicadas.
            4. Adapta la dificultad al nivel declarado.
            5. Los nombres de ejercicios deben ser claros y en español.
            6. Incluye series, repeticiones y descanso para cada ejercicio.
            7. Responde SOLO en JSON, sin texto adicional ni bloques de código.
            """;

    // ─── Generar 3 rutinas con IA ────────────────────────────────────

    @Transactional
    public GeneratedRoutinesResponse generateRoutines(String userId,
                                                      GenerateRoutineRequest request) {
        // Obtener ejercicios del catálogo como contexto para la IA
        String catalogContext = buildCatalogContext(request);
        String userPrompt = buildGenerationPrompt(request, catalogContext);

        log.info("Generando rutinas para usuario: {} con dificultad: {}",
                userId, request.getDifficulty());

        try {
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String rawResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            List<RoutineDto> routines = parseRoutinesFromResponse(rawResponse, request);

            String sessionId = UUID.randomUUID().toString();
            generatedSessions.put(sessionId, routines);

            log.info("3 rutinas generadas — sessionId: {}", sessionId);

            return GeneratedRoutinesResponse.builder()
                    .sessionId(sessionId)
                    .routines(routines)
                    .message("Selecciona una de las 3 rutinas generadas")
                    .build();

        } catch (Exception e) {
            log.error("Error generando rutinas con IA: {}", e.getMessage());
            throw new RuntimeException("Error al generar rutinas: " + e.getMessage(), e);
        }
    }

    // ─── Usuario selecciona una rutina ───────────────────────────────

    @Transactional
    public RoutineDto selectRoutine(String userId, SelectRoutineRequest request) {
        List<RoutineDto> routines = generatedSessions.get(request.getSessionId());
        if (routines == null) {
            throw new RuntimeException("Sesión expirada o inválida. Genera las rutinas nuevamente.");
        }

        // Verificar límite de 5 rutinas
        long activeRoutines = routineRepository.countByUserIdAndIsActiveTrue(userId);
        if (activeRoutines >= MAX_ROUTINES) {
            throw new RuntimeException(
                    "Has alcanzado el límite de " + MAX_ROUTINES + " rutinas activas. " +
                            "Elimina una antes de agregar otra.");
        }

        RoutineDto selected = routines.get(request.getSelectedIndex());
        String routineName = request.getCustomName() != null && !request.getCustomName().isBlank()
                ? request.getCustomName() : selected.getName();

        // Guardar la rutina
        Routine routine = new Routine();
        routine.setUserId(userId);
        routine.setName(routineName);
        routine.setDescription(selected.getDescription());
        routine.setDifficultyLevel(selected.getDifficultyLevel());
        routine.setEstimatedDurationMinutes(selected.getEstimatedDurationMinutes());
        routine.setAiGenerated(true);
        routine.setIsActive(true);

        Routine saved = routineRepository.save(routine);

        // Guardar ejercicios cruzando con el catálogo
        List<RoutineExercise> exercises = buildRoutineExercises(
                saved, selected.getExercises());
        routineExerciseRepository.saveAll(exercises);
        saved.setExercises(exercises);

        // Limpiar sesión
        generatedSessions.remove(request.getSessionId());

        log.info("Rutina seleccionada y guardada — id: {}, usuario: {}",
                saved.getId(), userId);

        return toDto(saved);
    }

    // ─── Obtener rutinas del usuario ─────────────────────────────────

    public List<RoutineDto> getUserRoutines(String userId) {
        return routineRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    public RoutineDto getRoutineById(String userId, UUID routineId) {
        return routineRepository.findByIdAndUserId(routineId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + routineId));
    }

    // ─── Agregar ejercicio manualmente ───────────────────────────────

    @Transactional
    public RoutineDto addExerciseToRoutine(String userId, UUID routineId,
                                           AddExerciseToRoutineRequest request) {
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + routineId));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException(
                        "Ejercicio no encontrado: " + request.getExerciseId()));

        int nextIndex = routine.getExercises().size();

        RoutineExercise re = new RoutineExercise();
        re.setRoutine(routine);
        re.setExercise(exercise);
        re.setSets(request.getSets());
        re.setReps(request.getReps());
        re.setWeightKg(request.getWeightKg());
        re.setRestSeconds(request.getRestSeconds() != null
                ? request.getRestSeconds() : exercise.getDefaultRestSeconds());
        re.setOrderIndex(nextIndex);
        re.setNotes(request.getNotes());

        routineExerciseRepository.save(re);
        routine.getExercises().add(re);

        log.info("Ejercicio '{}' agregado a rutina '{}'",
                exercise.getName(), routine.getName());

        return toDto(routine);
    }

    // ─── Editar ejercicio de rutina ──────────────────────────────────

    @Transactional
    public RoutineExerciseDto editRoutineExercise(String userId, UUID routineId,
                                                  UUID exerciseEntryId,
                                                  EditRoutineExerciseRequest request) {
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        RoutineExercise re = routineExerciseRepository.findById(exerciseEntryId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado en rutina"));

        if (!re.getRoutine().getId().equals(routine.getId())) {
            throw new RuntimeException("El ejercicio no pertenece a esta rutina");
        }

        if (request.getSets() != null) re.setSets(request.getSets());
        if (request.getReps() != null) re.setReps(request.getReps());
        if (request.getWeightKg() != null) re.setWeightKg(request.getWeightKg());
        if (request.getRestSeconds() != null) re.setRestSeconds(request.getRestSeconds());
        if (request.getNotes() != null) re.setNotes(request.getNotes());

        RoutineExercise saved = routineExerciseRepository.save(re);
        return toExerciseDto(saved);
    }

    // ─── Eliminar rutina ─────────────────────────────────────────────

    @Transactional
    public void deleteRoutine(String userId, UUID routineId) {
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + routineId));
        routine.setIsActive(false);
        routineRepository.save(routine);
        log.info("Rutina desactivada — id: {}, usuario: {}", routineId, userId);
    }

    // ─── Historial ───────────────────────────────────────────────────

    @Transactional
    public WorkoutHistoryDto logWorkout(String userId, UUID routineId, String notes) {
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        WorkoutHistory history = workoutHistoryRepository
                .findByUserIdAndWorkoutDate(userId, java.time.LocalDate.now())
                .orElseGet(() -> {
                    WorkoutHistory h = new WorkoutHistory();
                    h.setUserId(userId);
                    h.setWorkoutDate(java.time.LocalDate.now());
                    return h;
                });

        history.setRoutine(routine);
        history.setStatus(WorkoutHistory.WorkoutStatus.COMPLETADO);
        history.setCompletedAt(java.time.LocalDateTime.now());
        history.setNotes(notes);

        WorkoutHistory saved = workoutHistoryRepository.save(history);
        log.info("Entrenamiento registrado — usuario: {}, rutina: {}", userId, routineId);

        return toHistoryDto(saved);
    }

    public List<WorkoutHistoryDto> getHistory(String userId) {
        return workoutHistoryRepository
                .findByUserIdOrderByWorkoutDateDesc(userId)
                .stream().map(this::toHistoryDto).toList();
    }

    // ─── Helpers — construcción del prompt ───────────────────────────

    private String buildCatalogContext(GenerateRoutineRequest request) {
        List<String> muscleGroupNames = request.getMuscleGroups().stream()
                .flatMap(mg -> exerciseRepository
                        .findByMuscleGroupAndIsActiveTrue(mg).stream())
                .map(e -> e.getName() + " (" + e.getMuscleGroup() + ", " +
                        e.getDifficulty() + ", " + e.getEquipment() + ")")
                .distinct()
                .collect(Collectors.toList());

        if (muscleGroupNames.isEmpty()) return "";

        return "\n\nEjercicios disponibles en el catálogo (úsalos como referencia):\n" +
                String.join("\n", muscleGroupNames);
    }

    private String buildGenerationPrompt(GenerateRoutineRequest request,
                                         String catalogContext) {
        return """
                Genera exactamente 3 rutinas de entrenamiento diferentes con estas características:
                
                - Nivel de dificultad: %s
                - Grupos musculares: %s
                - Duración objetivo: %d minutos
                - Equipamiento disponible: %s
                - Limitaciones físicas: %s
                %s
                
                Responde con este JSON exacto (sin texto adicional):
                {
                  "routines": [
                    {
                      "name": "nombre de la rutina",
                      "description": "descripción breve",
                      "estimatedDurationMinutes": 45,
                      "exercises": [
                        {
                          "exerciseName": "nombre del ejercicio",
                          "muscleGroup": "PECHO",
                          "sets": 4,
                          "reps": 10,
                          "restSeconds": 90,
                          "notes": "observación opcional"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(
                request.getDifficulty(),
                request.getMuscleGroups().stream()
                        .map(Enum::name).collect(Collectors.joining(", ")),
                request.getDurationMinutes() != null ? request.getDurationMinutes() : 45,
                request.getAvailableEquipment() != null
                        ? request.getAvailableEquipment().stream()
                        .map(Enum::name).collect(Collectors.joining(", "))
                        : "cualquier equipamiento",
                request.getLimitations() != null ? request.getLimitations() : "ninguna",
                catalogContext
        );
    }

    // ─── Helpers — parseo de respuesta ───────────────────────────────

    @SuppressWarnings("unchecked")
    private List<RoutineDto> parseRoutinesFromResponse(String rawResponse,
                                                       GenerateRoutineRequest request) {
        try {
            String cleaned = rawResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);
            List<Map<String, Object>> routinesRaw =
                    (List<Map<String, Object>>) parsed.get("routines");

            return routinesRaw.stream()
                    .map(r -> parseRoutineDto(r, request))
                    .toList();

        } catch (Exception e) {
            log.error("Error parseando respuesta de IA: {}", e.getMessage());
            throw new RuntimeException("La IA devolvió un formato inválido", e);
        }
    }

    @SuppressWarnings("unchecked")
    private RoutineDto parseRoutineDto(Map<String, Object> raw,
                                       GenerateRoutineRequest request) {
        List<Map<String, Object>> exercisesRaw =
                (List<Map<String, Object>>) raw.get("exercises");

        List<RoutineExerciseDto> exercises = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> ex : exercisesRaw) {
            MuscleGroup mg;
            try {
                mg = MuscleGroup.valueOf(getString(ex, "muscleGroup"));
            } catch (Exception e) {
                mg = request.getMuscleGroups().get(0);
            }

            exercises.add(RoutineExerciseDto.builder()
                    .exerciseName(getString(ex, "exerciseName"))
                    .muscleGroup(mg)
                    .sets(getInt(ex, "sets"))
                    .reps(getInt(ex, "reps"))
                    .restSeconds(getInt(ex, "restSeconds"))
                    .notes(getString(ex, "notes"))
                    .orderIndex(index++)
                    .build());
        }

        return RoutineDto.builder()
                .name(getString(raw, "name"))
                .description(getString(raw, "description"))
                .difficultyLevel(request.getDifficulty())
                .estimatedDurationMinutes(getInt(raw, "estimatedDurationMinutes"))
                .aiGenerated(true)
                .exercises(exercises)
                .build();
    }

    // ─── Helpers — cruce con catálogo ────────────────────────────────

    private List<RoutineExercise> buildRoutineExercises(Routine routine,
                                                        List<RoutineExerciseDto> dtos) {
        List<RoutineExercise> result = new ArrayList<>();
        int index = 0;

        for (RoutineExerciseDto dto : dtos) {
            RoutineExercise re = new RoutineExercise();
            re.setRoutine(routine);
            re.setOrderIndex(index++);
            re.setSets(dto.getSets() != null ? dto.getSets() : 3);
            re.setReps(dto.getReps() != null ? dto.getReps() : 10);
            re.setRestSeconds(dto.getRestSeconds() != null ? dto.getRestSeconds() : 90);
            re.setNotes(dto.getNotes());

            // Cruzar con catálogo por nombre
            String exerciseName = dto.getExerciseName();
            Optional<Exercise> catalogMatch = exerciseRepository
                    .findByNameContainingIgnoreCaseAndIsActiveTrue(
                            exerciseName != null ? exerciseName : "")
                    .stream().findFirst();

            if (catalogMatch.isPresent()) {
                re.setExercise(catalogMatch.get());
                log.info("Ejercicio '{}' cruzado con catálogo", exerciseName);
            } else {
                // Crear ejercicio nuevo en el catálogo pendiente de revisión
                Exercise newExercise = new Exercise();
                newExercise.setName(exerciseName != null ? exerciseName : "Ejercicio sin nombre");
                newExercise.setMuscleGroup(dto.getMuscleGroup() != null
                        ? dto.getMuscleGroup() : Exercise.MuscleGroup.CUERPO_COMPLETO);
                newExercise.setEquipment(Exercise.Equipment.PESO_CORPORAL);
                newExercise.setDifficulty(routine.getDifficultyLevel() != null
                        ? routine.getDifficultyLevel() : Exercise.DifficultyLevel.INTERMEDIO);
                newExercise.setDefaultSets(re.getSets());
                newExercise.setDefaultReps(re.getReps());
                newExercise.setDefaultRestSeconds(re.getRestSeconds());
                newExercise.setIsActive(false); // pendiente de revisión del sistema
                Exercise saved = exerciseRepository.save(newExercise);
                re.setExercise(saved);
                log.info("Ejercicio '{}' agregado al catálogo pendiente de revisión",
                        exerciseName);
            }

            result.add(re);
        }

        return result;
    }

    // ─── Mappers ─────────────────────────────────────────────────────

    public RoutineDto toDto(Routine routine) {
        List<RoutineExerciseDto> exercises = routine.getExercises() != null
                ? routine.getExercises().stream()
                .map(this::toExerciseDto).toList()
                : List.of();

        return RoutineDto.builder()
                .id(routine.getId())
                .name(routine.getName())
                .description(routine.getDescription())
                .difficultyLevel(routine.getDifficultyLevel())
                .estimatedDurationMinutes(routine.getEstimatedDurationMinutes())
                .aiGenerated(routine.getAiGenerated())
                .createdAt(routine.getCreatedAt())
                .exercises(exercises)
                .build();
    }

    public RoutineExerciseDto toExerciseDto(RoutineExercise re) {
        return RoutineExerciseDto.builder()
                .id(re.getId())
                .exerciseId(re.getExercise().getId())
                .exerciseName(re.getExercise().getName())
                .muscleGroup(re.getExercise().getMuscleGroup())
                .sets(re.getSets())
                .reps(re.getReps())
                .weightKg(re.getWeightKg())
                .restSeconds(re.getRestSeconds())
                .orderIndex(re.getOrderIndex())
                .notes(re.getNotes())
                .build();
    }

    public WorkoutHistoryDto toHistoryDto(WorkoutHistory h) {
        return WorkoutHistoryDto.builder()
                .id(h.getId())
                .workoutDate(h.getWorkoutDate())
                .routineName(h.getRoutine().getName())
                .status(h.getStatus())
                .completedAt(h.getCompletedAt())
                .notes(h.getNotes())
                .build();
    }

    // ─── Helpers de parseo ────────────────────────────────────────────

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer i) return i;
        try { return Integer.parseInt(val.toString()); }
        catch (Exception e) { return null; }
    }
}