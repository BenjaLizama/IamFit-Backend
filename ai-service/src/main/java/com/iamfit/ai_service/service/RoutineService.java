package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.RoutineRequestDTO;
import com.iamfit.ai_service.dto.RoutineResponseDTO;
import com.iamfit.grpc.common.UserProfileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserProfileGrpcClient userProfileGrpcClient;
    private final RateLimitConfig rateLimitConfig;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., entrenadora personal virtual de iamfit.
        Generas rutinas de ejercicio semanales personalizadas.
        
        REGLAS:
        - Responde SIEMPRE en español.
        - Estructura la rutina por días de entrenamiento.
        - Cada día debe incluir: grupo muscular, ejercicios, series, repeticiones y descanso.
        - Respeta ESTRICTAMENTE las lesiones indicadas, evitando ejercicios que las agraven.
        - Aplica la regla de descanso muscular de al menos 48 horas por grupo muscular.
        - Adapta la intensidad al nivel y datos físicos del usuario.
        - Sé concisa y práctica.
        DISCLAIMER OBLIGATORIO:
        - Al final de CADA rutina generada agrega siempre esta nota:
           "⚠️ Importante: Esta rutina es una guía general y no reemplaza la evaluación 
        de un entrenador certificado o fisioterapeuta. Si tienes lesiones o condiciones 
         médicas, consulta siempre con un profesional antes de iniciar cualquier programa 
         de ejercicios."
        """;

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "generateRoutineFallback")
    public RoutineResponseDTO generateRoutine(String userId, RoutineRequestDTO request) {
        try {
            log.info("Generando rutina para usuario: {}", userId);

            // Rate limiting
            if (!rateLimitConfig.resolveBucket(userId).tryConsume(1)) {
                return RoutineResponseDTO.builder()
                        .status("RATE_LIMITED")
                        .rutina("Has alcanzado el límite de consultas por hora.")
                        .build();
            }

            // Obtener perfil del usuario
            String perfilContexto = buildPerfilContexto(userId);

            String userPrompt = buildPrompt(request, perfilContexto);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String rutina = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("aiCall service=routine userId={} status=SUCCESS",
                    userId.substring(0, 8) + "...");

            return RoutineResponseDTO.builder()
                    .userId(userId)
                    .rutina(rutina)
                    .objetivo(request.getObjetivo())
                    .nivel(request.getNivel())
                    .diasDisponibles(request.getDiasDisponibles())
                    .status("SUCCESS")
                    .disclaimer("Esta rutina es orientativa y no reemplaza " +
                            "la consulta con un profesional certificado.")
                    .build();

        } catch (Exception e) {
            log.error("Error al generar rutina: {}", e.getMessage());
            return RoutineResponseDTO.builder()
                    .userId(userId)
                    .rutina("Error al generar la rutina.")
                    .status("ERROR")
                    .build();
        }
    }

    public RoutineResponseDTO generateRoutineFallback(String userId,
                                                      RoutineRequestDTO request, Exception e) {
        log.error("Circuit breaker activado en RoutineService: {}", e.getMessage());
        return RoutineResponseDTO.builder()
                .userId(userId)
                .rutina("M.I.A. no está disponible. Intenta más tarde.")
                .status("CIRCUIT_OPEN")
                .build();
    }

    private String buildPerfilContexto(String userId) {
        try {
            UserProfileResponse profile = userProfileGrpcClient.getUserProfile(userId);
            if (!profile.getFound()) return "";
            return String.format("""
                Datos físicos del usuario:
                - Peso: %d kg
                - Altura: %d cm
                - Edad: %d años
                - Sexo: %s
                """,
                    profile.getWeight(),
                    profile.getHeight(),
                    profile.getAge(),
                    profile.getSex());
        } catch (Exception e) {
            log.warn("No se pudo obtener perfil para rutina: {}", e.getMessage());
            return "";
        }
    }

    private String buildPrompt(RoutineRequestDTO request, String perfilContexto) {
        List<String> lesiones = request.getLesiones();
        String lesionesTexto = (lesiones == null || lesiones.isEmpty())
                ? "Ninguna"
                : String.join(", ", lesiones);

        return String.format("""
                %s
                
                Genera una rutina de ejercicios semanal con las siguientes características:
                - Objetivo: %s
                - Días disponibles: %d días a la semana
                - Nivel: %s
                - Lesiones o limitaciones: %s
                
                Estructura la rutina día por día con ejercicios, series, repeticiones y descanso.
                """,
                perfilContexto,
                request.getObjetivo(),
                request.getDiasDisponibles(),
                request.getNivel(),
                lesionesTexto
        );
    }
}