package com.iamfit.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.ai_service.client.UserProfileGrpcClient;
import com.iamfit.ai_service.dto.AIResponseDTO;
import com.iamfit.ai_service.dto.MiaAction;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserProfileGrpcClient userProfileGrpcClient;
    private final ObjectMapper objectMapper;
    private final InternalContextService internalContextService;


    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
    Eres M.I.A. (My Intelligent Assistant), asistente virtual especializada
    exclusivamente en salud, nutrición y entrenamiento físico de la app iamfit.
    
    REGLAS ESTRICTAS:
    - Solo responde preguntas relacionadas con salud, nutrición, ejercicio y bienestar.
    - Si el usuario pregunta algo fuera de ese dominio, responde:
      "Solo soy experta en salud y fitness. ¿En qué te ayudo con tu entrenamiento hoy?"
    - Responde siempre en español.
    - Sé motivacional, empática y concisa.
    - Si tienes datos del usuario, úsalos para personalizar tu respuesta.
    
    FORMATO DE RESPUESTA OBLIGATORIO — responde SIEMPRE con este JSON exacto:
    {
      "content": "tu respuesta aquí",
      "actions": []
    }
    
    ACCIONES DISPONIBLES — incluye en "actions" solo cuando sea relevante:
    
    Si el usuario quiere crear una rutina:
    {
      "type": "CREATE_ROUTINE",
      "label": "Generar opciones de rutina",
      "payload": {
        "difficulty": "PRINCIPIANTE|INTERMEDIO|AVANZADO",
        "muscleGroups": ["PECHO","ESPALDA","HOMBROS","BICEPS","TRICEPS","PIERNAS","GLUTEOS","CORE","CARDIO","CUERPO_COMPLETO"],
        "availableEquipment": ["BARRA","MANCUERNAS","MAQUINA","POLEA","PESO_CORPORAL","BANDA_ELASTICA","KETTLEBELL"],
        "durationMinutes": 45,
        "limitations": "ninguna"
      }
    }
    
    Si el usuario quiere un plan de comidas:
    {
      "type": "GENERATE_MEAL_PLAN",
      "label": "Generar plan de comidas",
      "payload": {
        "goal": "Ganar músculo|Bajar de peso|Mantener peso",
        "preferences": [],
        "allergies": [],
        "likes": [],
        "dislikes": []
      }
    }
    
    Si el usuario menciona que comió algo específico:
    {
      "type": "ADD_FOOD",
      "label": "Registrar alimento",
      "payload": {
        "query": "nombre del alimento mencionado",
        "quantity": 100,
        "mealType": "DESAYUNO|ALMUERZO|CENA|SNACK"
      }
    }
    
    Si el usuario necesita motivación o apoyo emocional:
    {
      "type": "GET_MOTIVATION",
      "label": "Obtener motivación personalizada",
      "payload": { "contexto": "descripcion del contexto" }
    }
    
    DISCLAIMER OBLIGATORIO — agrega siempre al final del content:
    "\\n\\n⚠️ Esta información es orientativa y no reemplaza la consulta con un profesional certificado."
    
    IMPORTANTE: Responde SOLO el JSON. Sin texto adicional, sin bloques de código, sin markdown.
    """;

    @CircuitBreaker(name = "vertex-ai", fallbackMethod = "chatFallback")
    @Retry(name = "vertex-ai")
    public AIResponseDTO chat(String userId, String message, String token) {
        try {
            log.info("Procesando mensaje de usuario: {}", userId);

            String userContext = buildUserContext(userId);

            if (userContext.equals("[PERFIL_NO_DISPONIBLE]")) {
                return AIResponseDTO.builder()
                        .content("Para acceder a M.I.A. necesitas completar tu " +
                                "perfil primero. Ve a configuracion y completa " +
                                "tu informacion personal.")
                        .model(model)
                        .status("PROFILE_INCOMPLETE")
                        .actions(List.of())
                        .build();
            }

            // Contexto adicional de otros servicios
            String nutritionContext = internalContextService.getNutritionContext(token);
            String routinesContext = internalContextService.getRoutinesContext(token);
            String routineLimits = internalContextService.getRoutineLimitsContext(token);
            String mealPlanContext = internalContextService.getActiveMealPlanContext(token);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultAdvisors(MessageChatMemoryAdvisor
                            .builder(MessageWindowChatMemory.builder().build()).build())
                    .build();

            String fullMessage = userContext
                    + nutritionContext
                    + routinesContext
                    + routineLimits
                    + mealPlanContext
                    + "\n\nMensaje del usuario: " + message;

            String rawResponse = chatClient.prompt()
                    .user(fullMessage)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .content();

            log.info("aiCall userId={} status={} model={}",
                    userId != null ? userId.substring(0, 8) + "..." : "unknown",
                    "SUCCESS", model);

            return parseStructuredResponse(rawResponse, userId);

        } catch (Exception e) {
            log.error("Error al procesar mensaje: {}", e.getMessage());
            return AIResponseDTO.builder()
                    .content("Error al procesar tu consulta.")
                    .model(model)
                    .status("ERROR")
                    .actions(List.of())
                    .build();
        }
    }
    @SuppressWarnings("unchecked")
    private AIResponseDTO parseStructuredResponse(String rawResponse, String userId) {
        try {
            String cleaned = rawResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);

            String content = (String) parsed.getOrDefault("content", rawResponse);
            content = deseudonimizar(validateOutput(content));

            List<MiaAction> actions = new ArrayList<>();
            Object actionsRaw = parsed.get("actions");
            if (actionsRaw instanceof List<?> actionList) {
                for (Object actionObj : actionList) {
                    if (actionObj instanceof Map<?, ?> actionMap) {
                        actions.add(MiaAction.builder()
                                .type((String) actionMap.get("type"))
                                .label((String) actionMap.get("label"))
                                .payload(actionMap.get("payload"))
                                .build());
                    }
                }
            }

            return AIResponseDTO.builder()
                    .content(content)
                    .model(model)
                    .status("SUCCESS")
                    .disclaimer("Esta información es orientativa y no reemplaza " +
                            "la consulta con un profesional certificado.")
                    .actions(actions)
                    .build();

        } catch (Exception e) {
            log.warn("No se pudo parsear respuesta estructurada, retornando texto plano: {}", e.getMessage());
            return AIResponseDTO.builder()
                    .content(deseudonimizar(validateOutput(rawResponse)))
                    .model(model)
                    .status("SUCCESS")
                    .actions(List.of())
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

    public AIResponseDTO chatFallback(String userId, String message, String token, Exception e) {
        log.error("Circuit breaker activado para userId={}: {}",
                userId != null ? userId.substring(0, 8) + "..." : "unknown",
                e.getMessage());
        return AIResponseDTO.builder()
                .content("M.I.A. no esta disponible en este momento. Intenta mas tarde.")
                .model("unavailable")
                .status("CIRCUIT_OPEN")
                .actions(List.of())
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