package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.*;
import com.iamfit.grpc.common.UserProfileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellbeingService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserProfileGrpcClient userProfileGrpcClient;
    private final RateLimitConfig rateLimitConfig;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., asistente de bienestar integral de iamfit.
        Tu rol es apoyar el bienestar emocional y mental del usuario
        en el contexto de su salud y fitness.
        
        REGLAS ESTRICTAS:
        - Responde SIEMPRE en español.
        - Sé empática, cálida y motivacional.
        - Mantén un tono positivo pero realista.
        - Enfócate en bienestar general, no en diagnósticos clínicos.
        - Si detectas señales de crisis emocional grave, proporciona
          recursos de ayuda profesional INMEDIATAMENTE.
        - Nunca diagnostiques condiciones de salud mental.
        - Integra el contexto físico del usuario (ejercicio, sueño, nutrición)
          con su bienestar emocional cuando sea relevante.
        
        PROTOCOLO DE CRISIS:
        Si el usuario expresa pensamientos de autolesión, desesperanza extrema
        o crisis emocional grave, responde con empatía y proporciona:
        - Línea de la Vida Chile: 600 360 7777 (24/7, gratuita)
        - Fono Salud Responde: 600 360 7777
        - Sugiere buscar apoyo profesional inmediato.
        No intentes manejar la crisis por ti misma.
        
        DISCLAIMER OBLIGATORIO:
        Al final de cada respuesta agrega:
        "💙 Recuerda: M.I.A. es un asistente de bienestar y no reemplaza
        la atención de un profesional de salud mental. Si necesitas apoyo
        especializado, no dudes en consultar a un psicólogo o médico."
        """;

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "wellbeingFallback")
    public WellbeingResponseDTO checkIn(String userId, WellbeingCheckInRequestDTO request) {
        try {
            log.info("Procesando check-in de bienestar para userId: {}", userId);

            if (!rateLimitConfig.resolveBucket(userId).tryConsume(1)) {
                return WellbeingResponseDTO.builder()
                        .response("Has alcanzado el límite de consultas por hora.")
                        .tipo("CHECK_IN")
                        .status("RATE_LIMITED")
                        .build();
            }

            String perfilContexto = buildPerfilContexto(userId);
            String prompt = buildCheckInPrompt(request, perfilContexto);

            String response = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("aiCall service=wellbeing-checkin userId={} status=SUCCESS",
                    userId.substring(0, 8) + "...");

            return WellbeingResponseDTO.builder()
                    .response(response)
                    .tipo("CHECK_IN")
                    .status("SUCCESS")
                    .disclaimer("M.I.A. es un asistente de bienestar, " +
                            "no reemplaza atención profesional de salud mental.")
                    .build();

        } catch (Exception e) {
            log.error("Error en check-in de bienestar: {}", e.getMessage());
            return WellbeingResponseDTO.builder()
                    .response("Error al procesar tu check-in.")
                    .tipo("CHECK_IN")
                    .status("ERROR")
                    .build();
        }
    }

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "wellbeingFallback")
    public WellbeingResponseDTO getMotivation(String userId, MotivationRequestDTO request) {
        try {
            log.info("Generando motivación para userId: {}", userId);

            if (!rateLimitConfig.resolveBucket(userId).tryConsume(1)) {
                return WellbeingResponseDTO.builder()
                        .response("Has alcanzado el límite de consultas por hora.")
                        .tipo("MOTIVATION")
                        .status("RATE_LIMITED")
                        .build();
            }

            String perfilContexto = buildPerfilContexto(userId);
            String contexto = request.getContexto() != null
                    ? request.getContexto() : "general";

            String prompt = String.format("""
                %s
                
                El usuario necesita motivación para: %s
                
                Genera un mensaje motivacional personalizado, cálido y energizante
                de máximo 3 párrafos. Conecta el bienestar emocional con sus metas
                de fitness cuando tengas datos del perfil.
                """, perfilContexto, contexto);

            String response = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            return WellbeingResponseDTO.builder()
                    .response(response)
                    .tipo("MOTIVATION")
                    .status("SUCCESS")
                    .disclaimer("M.I.A. es un asistente de bienestar, " +
                            "no reemplaza atención profesional de salud mental.")
                    .build();

        } catch (Exception e) {
            log.error("Error generando motivación: {}", e.getMessage());
            return WellbeingResponseDTO.builder()
                    .response("Error al generar motivación.")
                    .tipo("MOTIVATION")
                    .status("ERROR")
                    .build();
        }
    }

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "wellbeingFallback")
    public WellbeingResponseDTO getTechnique(String userId, TechniqueRequestDTO request) {
        try {
            log.info("Generando técnica de bienestar para userId: {}", userId);

            if (!rateLimitConfig.resolveBucket(userId).tryConsume(1)) {
                return WellbeingResponseDTO.builder()
                        .response("Has alcanzado el límite de consultas por hora.")
                        .tipo("TECHNIQUE")
                        .status("RATE_LIMITED")
                        .build();
            }

            String tipo = request.getTipo() != null
                    ? request.getTipo() : "respiracion";

            String prompt = String.format("""
                Guía al usuario en una técnica de %s paso a paso.
                
                La técnica debe ser:
                - Práctica y ejecutable en 5-10 minutos
                - Explicada de forma clara y sencilla
                - Adaptada para alguien que hace ejercicio regularmente
                - Con beneficios específicos para el fitness y recuperación
                
                Incluye: nombre de la técnica, duración, pasos detallados
                y beneficios para el bienestar y el rendimiento físico.
                """, tipo);

            String response = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            return WellbeingResponseDTO.builder()
                    .response(response)
                    .tipo("TECHNIQUE")
                    .status("SUCCESS")
                    .disclaimer("M.I.A. es un asistente de bienestar, " +
                            "no reemplaza atención profesional de salud mental.")
                    .build();

        } catch (Exception e) {
            log.error("Error generando técnica: {}", e.getMessage());
            return WellbeingResponseDTO.builder()
                    .response("Error al generar la técnica.")
                    .tipo("TECHNIQUE")
                    .status("ERROR")
                    .build();
        }
    }

    public WellbeingResponseDTO wellbeingFallback(String userId,
                                                  Object request, Exception e) {
        log.error("Circuit breaker activado en WellbeingService: {}", e.getMessage());
        return WellbeingResponseDTO.builder()
                .response("M.I.A. no está disponible en este momento. Intenta más tarde.")
                .tipo("UNAVAILABLE")
                .status("CIRCUIT_OPEN")
                .build();
    }

    private String buildPerfilContexto(String userId) {
        try {
            UserProfileResponse profile = userProfileGrpcClient.getUserProfile(userId);
            if (!profile.getFound()) return "";
            return String.format("""
                [PERFIL DEL USUARIO]
                - Edad: %d años
                - Peso: %d kg
                - Altura: %d cm
                - Sexo: %s
                [FIN PERFIL]
                """,
                    profile.getAge(),
                    profile.getWeight(),
                    profile.getHeight(),
                    profile.getSex());
        } catch (Exception e) {
            log.warn("No se pudo obtener perfil para bienestar: {}", e.getMessage());
            return "";
        }
    }

    private String buildCheckInPrompt(WellbeingCheckInRequestDTO request,
                                      String perfilContexto) {
        String[] estadoTexto = {"", "muy mal", "mal", "regular", "bien", "excelente"};
        String[] estresTexto = {"", "sin estrés", "poco estresado",
                "algo estresado", "estresado", "muy estresado"};

        String notaTexto = (request.getNota() != null && !request.getNota().isBlank())
                ? "Nota del usuario: " + request.getNota()
                : "Sin nota adicional";

        return String.format("""
                %s
                
                El usuario reporta su estado de hoy:
                - Estado de ánimo: %s (%d/5)
                - Nivel de estrés: %s (%d/5)
                - %s
                
                Analiza su estado emocional y genera una respuesta empática que:
                1. Valide cómo se siente
                2. Conecte su estado emocional con su bienestar físico si tienes datos
                3. Sugiera 1-2 acciones concretas para mejorar su bienestar hoy
                4. Sea motivacional y esperanzadora
                
                Si detectas señales de crisis, aplica el protocolo de crisis inmediatamente.
                """,
                perfilContexto,
                estadoTexto[request.getEstadoAnimo()],
                request.getEstadoAnimo(),
                estresTexto[request.getNivelEstres()],
                request.getNivelEstres(),
                notaTexto
        );
    }
}