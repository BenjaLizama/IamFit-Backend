package com.iamfit.ejercicios.controller;

import com.iamfit.ejercicios.dto.*;
import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.service.ExerciseCatalogService;
import com.iamfit.ejercicios.service.RoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;
    private final ExerciseCatalogService exerciseCatalogService;

    // ─── Catálogo de ejercicios ──────────────────────────────────────

    @GetMapping("/exercises")
    public ResponseEntity<List<ExerciseDto>> getAllExercises() {
        return ResponseEntity.ok(exerciseCatalogService.getAll());
    }

    @GetMapping("/exercises/search")
    public ResponseEntity<List<ExerciseDto>> searchExercises(
            @RequestParam String name) {
        return ResponseEntity.ok(exerciseCatalogService.search(name));
    }

    @GetMapping("/exercises/filter")
    public ResponseEntity<List<ExerciseDto>> filterExercises(
            @RequestParam(required = false) MuscleGroup muscleGroup,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) Equipment equipment) {
        return ResponseEntity.ok(
                exerciseCatalogService.filter(muscleGroup, difficulty, equipment));
    }

    @GetMapping("/exercises/{id}")
    public ResponseEntity<ExerciseDto> getExerciseById(@PathVariable UUID id) {
        return ResponseEntity.ok(exerciseCatalogService.getById(id));
    }

    // ─── Generación de rutinas con IA ────────────────────────────────

    @PostMapping("/addRoutine")
    public ResponseEntity<GeneratedRoutinesResponse> generateRoutines(
            @Valid @RequestBody GenerateRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.generateRoutines(userId, request));
    }

    @PostMapping("/addRoutine/select")
    public ResponseEntity<RoutineDto> selectRoutine(
            @Valid @RequestBody SelectRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.selectRoutine(userId, request));
    }

    // ─── Gestión de rutinas ──────────────────────────────────────────



    @GetMapping("/routines/{routineId}")
    public ResponseEntity<RoutineDto> getRoutineById(
            @PathVariable UUID routineId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getRoutineById(userId, routineId));
    }

    @DeleteMapping("/routines/{routineId}")
    public ResponseEntity<Void> deleteRoutine(
            @PathVariable UUID routineId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        routineService.deleteRoutine(userId, routineId);
        return ResponseEntity.noContent().build();
    }

    // ─── Ejercicios dentro de una rutina ─────────────────────────────

    @PostMapping("/routines/{routineId}/exercises")
    public ResponseEntity<RoutineDto> addExerciseToRoutine(
            @PathVariable UUID routineId,
            @Valid @RequestBody AddExerciseToRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(
                routineService.addExerciseToRoutine(userId, routineId, request));
    }

    @PatchMapping("/editRoutine/{routineId}/exercises/{exerciseEntryId}")
    public ResponseEntity<RoutineExerciseDto> editRoutineExercise(
            @PathVariable UUID routineId,
            @PathVariable UUID exerciseEntryId,
            @RequestBody EditRoutineExerciseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(
                routineService.editRoutineExercise(userId, routineId,
                        exerciseEntryId, request));
    }

    // ─── Historial de entrenamientos ─────────────────────────────────

    @PostMapping("/routines/{routineId}/log")
    public ResponseEntity<WorkoutHistoryDto> logWorkout(
            @PathVariable UUID routineId,
            @RequestBody(required = false) LogWorkoutRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        LogWorkoutRequest safeRequest = request != null
                ? request : new LogWorkoutRequest(null, null, null, null);
        return ResponseEntity.ok(routineService.logWorkout(userId, routineId, safeRequest));
    }

    @GetMapping("/routines/history")
    public ResponseEntity<List<WorkoutHistoryDto>> getHistory(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getHistory(userId));
    }

    // ─── Filtrar por estado ──────────────────────────────────────────
    @GetMapping("/routines")
    public ResponseEntity<List<RoutineDto>> getUserRoutines(
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getUserRoutines(userId, status));
    }

    // ─── Actualizar metadata ─────────────────────────────────────────
    @PatchMapping("/routines/{routineId}")
    public ResponseEntity<RoutineDto> updateRoutine(
            @PathVariable UUID routineId,
            @RequestBody UpdateRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.updateRoutine(userId, routineId, request));
    }

    // ─── Activar / Desactivar ────────────────────────────────────────
    @PatchMapping("/routines/{routineId}/activate")
    public ResponseEntity<RoutineDto> activateRoutine(
            @PathVariable UUID routineId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.activateRoutine(userId, routineId));
    }

    @PatchMapping("/routines/{routineId}/deactivate")
    public ResponseEntity<RoutineDto> deactivateRoutine(
            @PathVariable UUID routineId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.deactivateRoutine(userId, routineId));
    }

    // ─── Eliminar ejercicio de rutina ────────────────────────────────
    @DeleteMapping("/routines/{routineId}/exercises/{exerciseEntryId}")
    public ResponseEntity<Void> deleteRoutineExercise(
            @PathVariable UUID routineId,
            @PathVariable UUID exerciseEntryId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        routineService.deleteRoutineExercise(userId, routineId, exerciseEntryId);
        return ResponseEntity.noContent().build();
    }

    // ─── Reordenar ejercicios ────────────────────────────────────────
    @PatchMapping("/routines/{routineId}/exercises/reorder")
    public ResponseEntity<RoutineDto> reorderExercises(
            @PathVariable UUID routineId,
            @RequestBody List<ReorderExerciseRequest> request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.reorderExercises(userId, routineId, request));
    }

    // ─── Limites ─────────────────────────────────────────────────────
    @GetMapping("/routines/limits")
    public ResponseEntity<RoutineLimitsDto> getRoutineLimits(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getRoutineLimits(userId));
    }

    // ─── Opciones del catalogo ───────────────────────────────────────
    @GetMapping("/exercises/options")
    public ResponseEntity<ExerciseOptionsDto> getExerciseOptions() {
        return ResponseEntity.ok(exerciseCatalogService.getOptions());
    }

    @GetMapping("/routines/history/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyWorkoutHistory(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getMonthlyWorkoutSummary(userId));
    }

    @GetMapping("/routines/history/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyWorkoutHistory(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getDailyWorkoutSummary(userId));
    }

    @GetMapping("/routines/history/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyWorkoutHistory(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getWeeklyWorkoutSummary(userId));
    }

    @PostMapping("/routines/{routineId}/sessions")
    public ResponseEntity<WorkoutSessionDto> startSession(
            @PathVariable UUID routineId,
            @RequestBody(required = false) StartSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        StartSessionRequest safeRequest = request != null ? request : new StartSessionRequest(null);
        return ResponseEntity.ok(routineService.startSession(userId, routineId, safeRequest));
    }

    @PatchMapping("/routines/{routineId}/sessions/{sessionId}/exercises/{exerciseEntryId}/complete")
    public ResponseEntity<SessionExerciseCompletionDto> completeSessionExercise(
            @PathVariable UUID routineId,
            @PathVariable UUID sessionId,
            @PathVariable UUID exerciseEntryId,
            @RequestBody(required = false) CompleteSessionExerciseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        CompleteSessionExerciseRequest safeRequest = request != null
                ? request : new CompleteSessionExerciseRequest(null, null, null, null);
        return ResponseEntity.ok(routineService.completeSessionExercise(
                userId, routineId, sessionId, exerciseEntryId, safeRequest));
    }

    @PatchMapping("/routines/{routineId}/sessions/{sessionId}/exercises/{exerciseEntryId}/uncomplete")
    public ResponseEntity<SessionExerciseCompletionDto> uncompleteSessionExercise(
            @PathVariable UUID routineId,
            @PathVariable UUID sessionId,
            @PathVariable UUID exerciseEntryId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.uncompleteSessionExercise(
                userId, routineId, sessionId, exerciseEntryId));
    }

    @GetMapping("/routines/{routineId}/progress")
    public ResponseEntity<RoutineProgressDto> getRoutineProgress(
            @PathVariable UUID routineId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("userId");
        return ResponseEntity.ok(routineService.getRoutineProgress(userId, routineId));
    }
}