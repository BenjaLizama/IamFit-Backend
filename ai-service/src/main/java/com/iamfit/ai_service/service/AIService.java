package com.iamfit.ai_service.service;

import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.dto.AIResponseDTO;
import com.iamfit.grpc.common.UserProfileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserProfileGrpcClient userProfileGrpcClient;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A. (My Intelligent Assistant), asistente virtual especializada
        exclusivamente en salud, nutrición y entrenamiento físico de la app iamfit.
        
        REGLAS ESTRICTAS:
        - Solo responde preguntas relacionadas con salud, nutrición, ejercicio y bienestar.
        - Si el usuario pregunta algo fuera de ese dominio, responde exactamente:
          "Solo soy experta en salud y fitness. ¿En qué te ayudo con tu entrenamiento hoy?"
        - Responde siempre en español.
        - Sé motivacional, empática y concisa.
        - Si tienes datos del usuario, úsalos para personalizar tu respuesta.
        DISCLAIMER OBLIGATORIO:
        - Al final de CADA respuesta agrega siempre esta nota:
        "⚠️ Recuerda: Esta información es orientativa y no reemplaza la consulta con 
         un médico, nutricionista o entrenador certificado. Ante cualquier duda o 
          condición médica, consulta siempre a un profesional de la salud."
        """;

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "chatFallback")
    @Retry(name = "vertex-ai")
    public AIResponseDTO chat(String userId, String message) {
        try {
            log.info("Procesando mensaje de usuario: {}", userId);

            String userContext = buildUserContext(userId);

            // ← Validación de perfil incompleto
            if (userContext.equals("[PERFIL_NO_DISPONIBLE]")) {
                return AIResponseDTO.builder()
                        .response("Para acceder a M.I.A. necesitas completar tu " +
                                "perfil primero. Ve a configuración y completa " +
                                "tu información personal.")
                        .model(model)
                        .status("PROFILE_INCOMPLETE")
                        .build();
            }

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultAdvisors(MessageChatMemoryAdvisor
                            .builder(MessageWindowChatMemory.builder().build()).build())
                    .build();

            String fullMessage = userContext.isBlank()
                    ? message
                    : userContext + "\n\nPregunta del usuario: " + message;

            String response = chatClient.prompt()
                    .user(fullMessage)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .content();

            String validatedResponse = validateOutput(response);

            String finalResponse = deseudonimizar(validatedResponse);

            log.info("aiCall userId={} status={} model={}",
                    userId != null ? userId.substring(0, 8) + "..." : "unknown",
                    "SUCCESS",
                    model);

            return AIResponseDTO.builder()
                    .response(finalResponse)
                    .model(model)
                    .status("SUCCESS")
                    .disclaimer("Esta rutina es orientativa y no reemplaza " +
                            "la consulta con un profesional certificado.")
                    .build();

        } catch (Exception e) {
            log.error("Error al procesar mensaje: {}", e.getMessage());
            return AIResponseDTO.builder()
                    .response("Error al procesar tu consulta.")
                    .model(model)
                    .status("ERROR")
                    .build();
        }
    }

    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    private String pseudonimizar(String valor, String tipo, String userId) {
        if (valor == null || valor.isBlank()) return "[DESCONOCIDO]";
        String token = "[" + tipo + "_" + userId.substring(0, 4).toUpperCase() + "]";
        tokenMap.put(token, valor);
        return token;
    }

    private String deseudonimizar(String texto) {
        for (Map.Entry<String, String> entry : tokenMap.entrySet()) {
            texto = texto.replace(entry.getKey(), entry.getValue());
        }
        return texto;
    }

    public AIResponseDTO chatFallback(String userId, String message, Exception e) {
        log.error("Circuit breaker activado para userId={}: {}",
                userId != null ? userId.substring(0, 8) + "..." : "unknown",
                e.getMessage());
        return AIResponseDTO.builder()
                .response("M.I.A. no está disponible en este momento. Intenta más tarde.")
                .model("unavailable")
                .status("CIRCUIT_OPEN")
                .build();
    }

    private String validateOutput(String output) {
        if (output == null || output.isBlank()) {
            return "No pude generar una respuesta. Intenta reformular tu pregunta.";
        }
        if (output.length() > 5000) {
            output = output.substring(0, 5000) + "...";
        }
        if (output.toLowerCase().contains("i cannot") ||
                output.toLowerCase().contains("i'm unable")) {
            return "No puedo responder esa consulta. " +
                    "¿Tienes alguna pregunta sobre salud o fitness?";
        }
        return output;
    }

    private String buildUserContext(String userId) {
        try {
            UserProfileResponse profile = userProfileGrpcClient.getUserProfile(userId);

            if (!profile.getFound()) {
                log.warn("Perfil no encontrado para userId: {}", userId);
                return "[PERFIL_NO_DISPONIBLE]";  // ← cambio clave
            }

            String nicknameToken = pseudonimizar(profile.getNickname(), "USUARIO", userId);

            return String.format("""
                [CONTEXTO DEL USUARIO - usa estos datos para personalizar tu respuesta]
                - Nickname: %s
                - Edad: %d años
                - Peso actual: %d kg
                - Altura: %d cm
                - Sexo: %s
                [FIN DEL CONTEXTO]
                """,
                    nicknameToken,
                    profile.getAge(),
                    profile.getWeight(),
                    profile.getHeight(),
                    profile.getSex()
            );

        } catch (Exception e) {
            log.warn("No se pudo obtener perfil gRPC para userId {}: {}",
                    userId, e.getMessage());
            return "[PERFIL_NO_DISPONIBLE]";
        }
    }
}