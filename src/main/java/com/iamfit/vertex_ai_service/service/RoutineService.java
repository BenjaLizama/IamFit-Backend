package com.iamfit.vertex_ai_service.service;


import com.iamfit.vertex_ai_service.dto.RoutineRequestDTO;
import com.iamfit.vertex_ai_service.dto.RoutineResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., entrenadora personal virtual de iamfit.
        Generas rutinas de ejercicio semanales personalizadas.
        
        REGLAS:
        - Responde SIEMPRE en español.
        - Estructura la rutina por días de entrenamiento.
        - Cada día debe incluir: grupo muscular, ejercicios, series, repeticiones y descanso.
        - Respeta ESTRICTAMENTE las lesiones indicadas, evitando ejercicios que las agraven.
        - Aplica la regla de descanso muscular de al menos 48 horas por grupo muscular.
        - Adapta la intensidad y complejidad al nivel del usuario.
        - Sé concisa y práctica.
        """;

    public RoutineResponseDTO generateRoutine(RoutineRequestDTO request) {
        try {
            log.info("Generando rutina para usuario: {}", request.getUserId());

            String userPrompt = buildPrompt(request);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String rutina = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Rutina generada exitosamente para usuario: {}", request.getUserId());

            return RoutineResponseDTO.builder()
                    .userId(request.getUserId())
                    .rutina(rutina)
                    .objetivo(request.getObjetivo())
                    .nivel(request.getNivel())
                    .diasDisponibles(request.getDiasDisponibles())
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Error al generar rutina: {}", e.getMessage());
            return RoutineResponseDTO.builder()
                    .userId(request.getUserId())
                    .rutina("Error al generar la rutina.")
                    .status("ERROR")
                    .build();
        }
    }

    private String buildPrompt(RoutineRequestDTO request) {
        List<String> lesiones = request.getLesiones();

        String lesionesTexto = (lesiones == null || lesiones.isEmpty())
                ? "Ninguna"
                : String.join(", ", lesiones);

        return String.format("""
                Genera una rutina de ejercicios semanal con las siguientes características:
                
                - Objetivo: %s
                - Días disponibles para entrenar: %d días a la semana
                - Nivel: %s
                - Lesiones o limitaciones físicas: %s
                
                Estructura la rutina día por día con ejercicios, series, repeticiones y tiempo de descanso.
                """,
                request.getObjetivo(),
                request.getDiasDisponibles(),
                request.getNivel(),
                lesionesTexto
        );
    }
}