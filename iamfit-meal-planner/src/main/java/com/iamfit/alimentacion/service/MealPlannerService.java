package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.MealPlanResponse;
import com.iamfit.alimentacion.dto.UserPreferencesRequest;
import com.iamfit.alimentacion.exception.MealPlanGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlannerService {

    // 1. Inyectamos el BUILDER, igual que en ai-service
    private final ChatClient.Builder chatClientBuilder;

    // ¡ELIMINAMOS el ObjectMapper! Ya no lo necesitamos.

    private static final String SYSTEM_PROMPT = """
            Actúa como un experto en nutrición y fitness altamente calificado.
            Tu objetivo es generar un plan de comidas semanal optimizado para el usuario.
            
            Reglas estrictas:
            1. Respeta absolutamente las alergias y disgustos.
            2. Ajusta el valor calórico según el objetivo declarado.
            """;

    public MealPlanResponse generateMealPlan(UserPreferencesRequest request) {
        try {
            // 2. Usamos el conversor mágico de Spring AI
            var outputConverter = new BeanOutputConverter<>(MealPlanResponse.class);

            // 3. Esto le dice automáticamente a la IA cómo debe ser el JSON
            String formatInstructions = outputConverter.getFormat();

            String userPrompt = buildUserPrompt(request) + "\n\n" + formatInstructions;

            log.debug("Sending prompt to AI...");

            // 4. Construimos el cliente y llamamos a la IA
            ChatClient chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();

            String rawResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            // 5. El conversor hace el parseo, limpia el markdown y maneja errores por ti
            return outputConverter.convert(rawResponse);

        } catch (Exception ex) {
            log.error("Error communicating with the AI provider or parsing response", ex);
            throw new MealPlanGenerationException("Error al generar el plan de comidas: " + ex.getMessage(), ex);
        }
    }

    private String buildUserPrompt(UserPreferencesRequest req) {
        return """
                Genera mi plan de comidas con:
                - Objetivo: %s
                - Preferencias: %s
                - Alergias (EXCLUIR): %s
                - Gustos: %s
                - Disgustos (EXCLUIR): %s
                """.formatted(
                req.getGoal(),
                listToString(req.getPreferences()),
                listToString(req.getAllergies()),
                listToString(req.getLikes()),
                listToString(req.getDislikes())
        );
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "ninguno";
        return String.join(", ", list);
    }
}