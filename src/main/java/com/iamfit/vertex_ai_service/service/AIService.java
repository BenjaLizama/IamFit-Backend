package com.iamfit.vertex_ai_service.service;

import com.iamfit.vertex_ai_service.dto.AIResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient.Builder chatClientBuilder;

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
        """;

    public AIResponseDTO chat(String userId, String message) {
        try {
            log.info("Procesando mensaje de usuario: {}", userId);

            ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                    .build();

            String response = chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .content();

            log.info("Respuesta generada exitosamente para usuario: {}", userId);

            return AIResponseDTO.builder()
                    .response(response)
                    .model(model)
                    .status("SUCCESS")
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
}