package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.configuration.RateLimitConfig;
import com.iamfit.ai_service.dto.WeeklyMenuRequestDTO;
import com.iamfit.ai_service.dto.WeeklyMenuResponseDTO;
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
public class WeeklyMenuService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserProfileGrpcClient userProfileGrpcClient;
    private final RateLimitConfig rateLimitConfig;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., nutricionista virtual de iamfit.
        Generas menús semanales personalizados.
        
        REGLAS:
        - Responde SIEMPRE en español.
        - Estructura el menú de Lunes a Domingo.
        - Cada día incluye: Desayuno, Almuerzo, Cena y 1 Snack.
        - Respeta ESTRICTAMENTE las alergias o intolerancias.
        - Ajusta las porciones al objetivo calórico indicado.
        - Considera el peso y altura del usuario para las porciones.
        - Sé concisa y práctica.
        DISCLAIMER OBLIGATORIO:
         - Al final de CADA menú generado agrega siempre esta nota:
        "⚠️ Importante: Este menú es una guía orientativa y no reemplaza la consulta 
        con un nutricionista o médico certificado. Las necesidades nutricionales varían 
        según cada persona. Ante alergias graves o condiciones médicas, consulta siempre 
        con un profesional de la salud."
        """;

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "generateMenuFallback")
    public WeeklyMenuResponseDTO generateMenu(String userId, WeeklyMenuRequestDTO request) {
        try {
            log.info("Generando menú para usuario: {}", userId);

            // Rate limiting
            if (!rateLimitConfig.resolveBucket(userId).tryConsume(1)) {
                return WeeklyMenuResponseDTO.builder()
                        .status("RATE_LIMITED")
                        .menu("Has alcanzado el límite de consultas por hora.")
                        .build();
            }

            // Obtener perfil del usuario
            String perfilContexto = buildPerfilContexto(userId);

            String userPrompt = buildPrompt(request, perfilContexto);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String menu = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("aiCall service=menu userId={} status=SUCCESS",
                    userId.substring(0, 8) + "...");

            return WeeklyMenuResponseDTO.builder()
                    .userId(userId)
                    .menu(menu)
                    .objetivo(request.getObjetivo())
                    .calorias(request.getCalorias())
                    .status("SUCCESS")
                    .disclaimer("Esta rutina es orientativa y no reemplaza " +
                            "la consulta con un profesional certificado.")
                    .build();

        } catch (Exception e) {
            log.error("Error al generar menú: {}", e.getMessage());
            return WeeklyMenuResponseDTO.builder()
                    .userId(userId)
                    .menu("Error al generar el menú semanal.")
                    .status("ERROR")
                    .build();
        }
    }

    public WeeklyMenuResponseDTO generateMenuFallback(String userId,
                                                      WeeklyMenuRequestDTO request, Exception e) {
        log.error("Circuit breaker activado en WeeklyMenuService: {}", e.getMessage());
        return WeeklyMenuResponseDTO.builder()
                .userId(userId)
                .menu("M.I.A. no está disponible. Intenta más tarde.")
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
            log.warn("No se pudo obtener perfil para menú: {}", e.getMessage());
            return "";
        }
    }

    private String buildPrompt(WeeklyMenuRequestDTO request, String perfilContexto) {
        List<String> alergias = request.getAlergias();
        String alergiasTexto = (alergias == null || alergias.isEmpty())
                ? "Ninguna"
                : String.join(", ", alergias);

        return String.format("""
                %s
                
                Genera un menú semanal completo (Lunes a Domingo):
                - Objetivo: %s peso
                - Calorías diarias: %d kcal
                - Alergias o intolerancias: %s
                
                Incluye Desayuno, Almuerzo, Cena y 1 Snack por día.
                """,
                perfilContexto,
                request.getObjetivo(),
                request.getCalorias(),
                alergiasTexto
        );
    }
}