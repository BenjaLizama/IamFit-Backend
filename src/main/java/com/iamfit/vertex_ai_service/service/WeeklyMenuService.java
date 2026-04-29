package com.iamfit.vertex_ai_service.service;

import com.iamfit.vertex_ai_service.dto.WeeklyMenuRequestDTO;
import com.iamfit.vertex_ai_service.dto.WeeklyMenuResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyMenuService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., nutricionista virtual de iamfit.
        Generas menús semanales personalizados basados en el objetivo,
        calorías diarias y restricciones alimentarias del usuario.
        
        REGLAS:
        - Responde SIEMPRE en español.
        - Estructura el menú por días (Lunes a Domingo).
        - Cada día debe incluir: Desayuno, Almuerzo, Cena y 1 Snack.
        - Respeta ESTRICTAMENTE las alergias o intolerancias indicadas.
        - Ajusta las porciones para alcanzar las calorías diarias indicadas.
        - Sé concisa y práctica, sin explicaciones largas.
        """;

    public WeeklyMenuResponseDTO generateMenu(WeeklyMenuRequestDTO request) {
        try {
            log.info("Generando menú semanal para usuario: {}", request.getUserId());

            String userPrompt = buildPrompt(request);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String menu = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Menú generado exitosamente para usuario: {}", request.getUserId());

            return WeeklyMenuResponseDTO.builder()
                    .userId(request.getUserId())
                    .menu(menu)
                    .objetivo(request.getObjetivo())
                    .calorias(request.getCalorias())
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Error al generar menú: {}", e.getMessage());
            return WeeklyMenuResponseDTO.builder()
                    .userId(request.getUserId())
                    .menu("Error al generar el menú semanal.")
                    .status("ERROR")
                    .build();
        }
    }

    private String buildPrompt(WeeklyMenuRequestDTO request) {
        List<String> alergias = request.getAlergias();

        String alergiasTexto = (alergias == null || alergias.isEmpty())
                ? "Ninguna"
                : String.join(", ", alergias);

        return String.format("""
                Genera un menú semanal completo (Lunes a Domingo) con las siguientes características:
                
                - Objetivo: %s peso
                - Calorías diarias objetivo: %d kcal
                - Alergias o intolerancias: %s
                
                Incluye Desayuno, Almuerzo, Cena y 1 Snack por día.
                """,
                request.getObjetivo(),
                request.getCalorias(),
                alergiasTexto
        );
    }
}