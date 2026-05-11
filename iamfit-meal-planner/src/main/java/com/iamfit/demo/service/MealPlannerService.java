package com.iamfit.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.demo.dto.MealPlanResponse;
import com.iamfit.demo.dto.UserPreferencesRequest;
import com.iamfit.demo.exception.MealPlanGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlannerService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // -------------------------------------------------------
    //  System prompt — the AI's "brain"
    // -------------------------------------------------------
    private static final String SYSTEM_PROMPT = """
            Actúa como un experto en nutrición y fitness altamente calificado.
            Tu objetivo es generar un plan de comidas semanal (de lunes a domingo) optimizado para el usuario.

            Formato de salida requerido: Devuelve la respuesta EXCLUSIVAMENTE en formato JSON puro con la siguiente estructura:
            {
              "objetivo": "string",
              "menu": {
                "lunes":   { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "martes":  { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "miercoles":{ "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "jueves":  { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "viernes": { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "sabado":  { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] },
                "domingo": { "desayuno": "", "almuerzo": "", "cena": "", "snacks": [] }
              },
              "recomendaciones_nutricionales": "string"
            }

            Reglas estrictas:
            1. Respeta absolutamente las alergias y disgustos — ninguno de esos alimentos puede aparecer.
            2. Ajusta el valor calórico y los macronutrientes según el objetivo declarado.
            3. No incluyas texto introductorio, explicaciones ni bloques de código — solo el JSON puro.
            4. Todos los nombres de los días van en minúsculas y sin tildes (lunes, martes, ...).
            """;

    // -------------------------------------------------------
    //  Public API
    // -------------------------------------------------------

    /**
     * Generates a weekly meal plan based on the user's preferences.
     *
     * @param request user dietary preferences and goals
     * @return parsed {@link MealPlanResponse}
     */
    public MealPlanResponse generateMealPlan(UserPreferencesRequest request) {
        String userPrompt = buildUserPrompt(request);
        log.debug("Sending prompt to AI:\n{}", userPrompt);

        String rawResponse = callAi(userPrompt);
        log.debug("Raw AI response:\n{}", rawResponse);

        return parseResponse(rawResponse);
    }

    // -------------------------------------------------------
    //  Private helpers
    // -------------------------------------------------------

    /** Converts the DTO into a readable user prompt. */
    private String buildUserPrompt(UserPreferencesRequest req) {
        return """
                Por favor genera mi plan de comidas semanal con los siguientes datos:

                - Objetivo: %s
                - Preferencias dietéticas: %s
                - Alergias (EXCLUIR SIEMPRE): %s
                - Alimentos que me gustan (incluir si es posible): %s
                - Alimentos que no me gustan (EXCLUIR SIEMPRE): %s
                """.formatted(
                req.getGoal(),
                listToString(req.getPreferences()),
                listToString(req.getAllergies()),
                listToString(req.getLikes()),
                listToString(req.getDislikes())
        );
    }

    /** Calls the AI via Spring AI's ChatClient. */
    private String callAi(String userPrompt) {
        try {
            return chatClient
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Error communicating with the AI provider", ex);
            throw new MealPlanGenerationException("No se pudo contactar con el servicio de IA: " + ex.getMessage(), ex);
        }
    }

    /** Parses the raw JSON string returned by the AI into a typed DTO. */
    private MealPlanResponse parseResponse(String rawJson) {
        // Strip accidental markdown fences the model might add despite instructions
        String cleaned = rawJson
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, MealPlanResponse.class);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse AI response as JSON. Raw content:\n{}", rawJson, ex);
            throw new MealPlanGenerationException(
                    "La IA devolvió una respuesta que no es JSON válido. Intenta nuevamente.", ex);
        }
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "ninguno";
        }
        return String.join(", ", list);
    }
}