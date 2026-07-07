package com.iamfit.ejercicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ejercicios.config.SecurityConfig;
import com.iamfit.ejercicios.dto.*;
import com.iamfit.ejercicios.entity.Exercise.DifficultyLevel;
import com.iamfit.ejercicios.entity.Exercise.Equipment;
import com.iamfit.ejercicios.entity.Exercise.MuscleGroup;
import com.iamfit.ejercicios.service.ExerciseCatalogService;
import com.iamfit.ejercicios.service.RoutineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RoutineController.class,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@Import(SecurityConfig.class)
class RoutineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // USAMOS EL OBJECTMAPPER REAL DE SPRING: Soluciona los problemas de serialización de Enums y fechas
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // Esto carga automáticamente soporte para Java 8 Time, Enums, etc.

    @MockitoBean
    private RoutineService routineService;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @MockitoBean
    private ExerciseCatalogService exerciseCatalogService;

    private final String testUserId = "user-123";

    /**
     * Construye un objeto JWT falso que simula perfectamente el @AuthenticationPrincipal
     */
    private Jwt mockJwtToken() {
        return Jwt.withTokenValue("mock-token-xyz")
                .header("alg", "none")
                .claim("userId", testUserId)
                .build();
    }

    // ─── CATÁLOGO DE EJERCICIOS (STATUS 200 OK) ─────────────────────────

    @Test
    @DisplayName("GET /exercises - Retorna lista completa")
    void getAllExercises_returns200() throws Exception {
        when(exerciseCatalogService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/exercises")
                        .with(jwt().jwt(mockJwtToken()))) // Añadido el token JWT
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /exercises/search - Busca por nombre")
    void searchExercises_returns200() throws Exception {
        when(exerciseCatalogService.search("press")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/exercises/search")
                        .with(jwt().jwt(mockJwtToken())) // Añadido el token JWT
                        .param("name", "press"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /exercises/filter - Filtra ejercicios por criterios")
    void filterExercises_returns200() throws Exception {
        when(exerciseCatalogService.filter(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/exercises/filter")
                        .with(jwt().jwt(mockJwtToken())) // Añadido el token JWT
                        .param("muscleGroup", "PECHO")
                        .param("difficulty", "PRINCIPIANTE")
                        .param("equipment", "BARRA"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /exercises/{id} - Busca ejercicio por ID")
    void getExerciseById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(exerciseCatalogService.getById(id)).thenReturn(ExerciseDto.builder().build());

        mockMvc.perform(get("/api/v1/exercises/{id}", id)
                        .with(jwt().jwt(mockJwtToken()))) // Añadido el token JWT
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /exercises/options - Retorna enums del catálogo")
    void getExerciseOptions_returns200() throws Exception {
        when(exerciseCatalogService.getOptions()).thenReturn(ExerciseOptionsDto.builder().build());

        mockMvc.perform(get("/api/v1/exercises/options")
                        .with(jwt().jwt(mockJwtToken()))) // Añadido el token JWT
                .andExpect(status().isOk());
    }
    // ─── GENERACIÓN Y GESTIÓN DE RUTINAS (STATUS CORRECTOS) ─────────────

    @Test
    @DisplayName("POST /addRoutine - Genera rutina con IA (200 OK)")
    void generateRoutine_returns200() throws Exception {
        GenerateRoutineRequest request = new GenerateRoutineRequest();
        request.setDifficulty(DifficultyLevel.PRINCIPIANTE);
        request.setMuscleGroups(List.of(MuscleGroup.PECHO));

        when(routineService.generateRoutines(eq(testUserId), any(GenerateRoutineRequest.class)))
                .thenReturn(GeneratedRoutinesResponse.builder().build());

        mockMvc.perform(post("/api/v1/addRoutine")
                        .with(jwt().jwt(mockJwtToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /addRoutine/select - Guarda la rutina elegida de la IA (200 OK)")
    void selectRoutine_returns200() throws Exception {
        SelectRoutineRequest request = new SelectRoutineRequest();
        request.setSessionId("session-xyz");
        request.setSelectedIndex(0);

        when(routineService.selectRoutine(eq(testUserId), any(SelectRoutineRequest.class)))
                .thenReturn(RoutineDto.builder().build());

        mockMvc.perform(post("/api/v1/addRoutine/select")
                        .with(jwt().jwt(mockJwtToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines/{id} - Obtiene rutina específica (200 OK)")
    void getRoutineById_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        when(routineService.getRoutineById(testUserId, routineId)).thenReturn(RoutineDto.builder().build());

        mockMvc.perform(get("/api/v1/routines/{routineId}", routineId)
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /routines/{id} - Borrado lógico de rutina (204 No Content)")
    void deleteRoutine_returns204() throws Exception {
        UUID routineId = UUID.randomUUID();
        doNothing().when(routineService).deleteRoutine(testUserId, routineId);

        mockMvc.perform(delete("/api/v1/routines/{routineId}", routineId)
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isNoContent());
    }

    // ─── EJERCICIOS DENTRO DE UNA RUTINA (STATUS CORRECTOS) ────────────────

    @Test
    @DisplayName("POST /routines/{id}/exercises - Añade ejercicio manual (200 OK)")
    void addExerciseToRoutine_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        AddExerciseToRoutineRequest request = new AddExerciseToRoutineRequest();
        request.setExerciseId(UUID.randomUUID());
        request.setSets(4);
        request.getReps(); request.setReps(12);

        when(routineService.addExerciseToRoutine(eq(testUserId), eq(routineId), any(AddExerciseToRoutineRequest.class)))
                .thenReturn(RoutineDto.builder().build());

        mockMvc.perform(post("/api/v1/routines/{routineId}/exercises", routineId)
                        .with(jwt().jwt(mockJwtToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /editRoutine/{id}/exercises/{entryId} - Edita ejercicio (200 OK)")
    void editRoutineExercise_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        EditRoutineExerciseRequest request = new EditRoutineExerciseRequest();
        request.setSets(3);

        when(routineService.editRoutineExercise(eq(testUserId), eq(routineId), eq(entryId), any(EditRoutineExerciseRequest.class)))
                .thenReturn(RoutineExerciseDto.builder().build());

        mockMvc.perform(patch("/api/v1/editRoutine/{routineId}/exercises/{exerciseEntryId}", routineId, entryId)
                        .with(jwt().jwt(mockJwtToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /routines/{id}/exercises/{entryId} - Elimina ejercicio de rutina (204 No Content)")
    void deleteRoutineExercise_returns204() throws Exception {
        UUID routineId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        doNothing().when(routineService).deleteRoutineExercise(testUserId, routineId, entryId);

        mockMvc.perform(delete("/api/v1/routines/{routineId}/exercises/{exerciseEntryId}", routineId, entryId)
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /routines/{id}/exercises/reorder - Reordena ejercicios (200 OK)")
    void reorderExercises_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        ReorderExerciseRequest order = new ReorderExerciseRequest();
        order.setExerciseEntryId(UUID.randomUUID());
        order.setNewOrderIndex(1);

        List<ReorderExerciseRequest> listRequest = List.of(order);

        when(routineService.reorderExercises(eq(testUserId), eq(routineId), any(List.class)))
                .thenReturn(RoutineDto.builder().build());

        mockMvc.perform(patch("/api/v1/routines/{routineId}/exercises/reorder", routineId)
                        .with(jwt().jwt(mockJwtToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listRequest)))
                .andExpect(status().isOk());
    }

    // ─── GESTIÓN DE HISTORIAL Y METADATA (STATUS CORRECTOS) ────────────────

    @Test
    @DisplayName("POST /routines/{id}/log - Registra un entrenamiento finalizado (200 OK)")
    void logWorkout_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        when(routineService.logWorkout(testUserId, routineId, "Excelente entreno")).thenReturn(WorkoutHistoryDto.builder().build());

        mockMvc.perform(post("/api/v1/routines/{routineId}/log", routineId)
                        .with(jwt().jwt(mockJwtToken()))
                        .param("notes", "Excelente entreno"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines/history - Retorna historial de entrenamientos (200 OK)")
    void getHistory_returns200() throws Exception {
        when(routineService.getHistory(testUserId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/routines/history")
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines - Lista rutinas filtrando por estado (200 OK)")
    void getUserRoutines_returns200() throws Exception {
        when(routineService.getUserRoutines(testUserId, "ACTIVE")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/routines")
                        .with(jwt().jwt(mockJwtToken()))
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /routines/{id}/activate - Activa rutina (200 OK)")
    void activateRoutine_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        when(routineService.activateRoutine(testUserId, routineId)).thenReturn(RoutineDto.builder().build());

        mockMvc.perform(patch("/api/v1/routines/{routineId}/activate", routineId)
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /routines/{id}/deactivate - Desactiva rutina (200 OK)")
    void deactivateRoutine_returns200() throws Exception {
        UUID routineId = UUID.randomUUID();
        when(routineService.deactivateRoutine(testUserId, routineId)).thenReturn(RoutineDto.builder().build());

        mockMvc.perform(patch("/api/v1/routines/{routineId}/deactivate", routineId)
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines/limits - Obtiene límites de creación (200 OK)")
    void getRoutineLimits_returns200() throws Exception {
        when(routineService.getRoutineLimits(testUserId)).thenReturn(RoutineLimitsDto.builder().build());

        mockMvc.perform(get("/api/v1/routines/limits")
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    // ─── ENDPOINTS DE MÉTRICAS HISTÓRICOS (STATUS 200 OK) ────────────────

    @Test
    @DisplayName("GET /routines/history/monthly - Historial Mensual (200 OK)")
    void getMonthlyWorkoutHistory_returns200() throws Exception {
        when(routineService.getMonthlyWorkoutSummary(testUserId)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/routines/history/monthly")
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines/history/daily - Historial Diario (200 OK)")
    void getDailyWorkoutHistory_returns200() throws Exception {
        when(routineService.getDailyWorkoutSummary(testUserId)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/routines/history/daily")
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /routines/history/weekly - Historial Semanal (200 OK)")
    void getWeeklyWorkoutHistory_returns200() throws Exception {
        when(routineService.getWeeklyWorkoutSummary(testUserId)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/routines/history/weekly")
                        .with(jwt().jwt(mockJwtToken())))
                .andExpect(status().isOk());
    }
}