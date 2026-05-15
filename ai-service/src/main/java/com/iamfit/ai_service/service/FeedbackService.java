package com.iamfit.ai_service.service;

import com.iamfit.ai_service.dto.FeedbackRequestDTO;
import com.iamfit.ai_service.dto.FeedbackResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
        Eres M.I.A., asistente de salud y fitness de iamfit.
        Analizas las estadísticas del usuario y generas retroalimentación
        personalizada, motivacional y accionable.
        
        REGLAS:
        - Responde SIEMPRE en español.
        - Analiza tanto las estadísticas diarias como las semanales.
        - Identifica puntos positivos y áreas de mejora.
        - Da recomendaciones concretas y alcanzables para los próximos días.
        - Sé empática, motivacional y directa.
        - Nunca des diagnósticos médicos.
        - Estructura tu respuesta en secciones claras:
          1. Resumen del día
          2. Resumen de la semana
          3. Puntos positivos
          4. Áreas de mejora
          5. Recomendaciones para mañana
        """;

    public FeedbackResponseDTO generateFeedback(FeedbackRequestDTO request) {
        try {
            log.info("Generando feedback para usuario: {}", request.getUserId());

            String userPrompt = buildPrompt(request);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .build();

            String feedback = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Feedback generado exitosamente para usuario: {}", request.getUserId());

            return FeedbackResponseDTO.builder()
                    .userId(request.getUserId())
                    .feedback(feedback)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Error al generar feedback: {}", e.getMessage());
            return FeedbackResponseDTO.builder()
                    .userId(request.getUserId())
                    .feedback("Error al generar el feedback.")
                    .status("ERROR")
                    .build();
        }
    }

    private String buildPrompt(FeedbackRequestDTO request) {
        List<String> ejercicios = request.getEjerciciosRealizados();

        String ejerciciosTexto = (ejercicios == null || ejercicios.isEmpty())
                ? "Ninguno registrado"
                : String.join(", ", ejercicios);

        double diferenciaPeso = request.getPesoActual() - request.getPesoInicialSemana();
        String tendenciaPeso = diferenciaPeso < 0
                ? String.format("bajó %.1f kg", Math.abs(diferenciaPeso))
                : diferenciaPeso > 0
                ? String.format("subió %.1f kg", diferenciaPeso)
                : "se mantuvo igual";

        return String.format("""
                Genera una retroalimentación personalizada basada en las siguientes estadísticas:
                
                **OBJETIVO DEL USUARIO:** %s peso
                
                **ESTADÍSTICAS DE HOY:**
                - Peso actual: %.1f kg
                - Calorías consumidas: %d kcal
                - Agua consumida: %.1f litros
                - Horas de sueño: %.1f horas
                - Ejercicios realizados: %s
                
                **ESTADÍSTICAS DE LA SEMANA:**
                - Peso inicio de semana: %.1f kg → Peso actual: %.1f kg (%s)
                - Promedio de calorías diarias: %d kcal
                - Promedio de agua diaria: %.1f litros
                - Promedio de sueño diario: %.1f horas
                - Días entrenados esta semana: %d días
                
                Proporciona feedback honesto, motivacional y con recomendaciones concretas.
                """,
                request.getObjetivo(),
                request.getPesoActual(),
                request.getCaloriasConsumidas(),
                request.getAguaConsumida(),
                request.getHorasSueno(),
                ejerciciosTexto,
                request.getPesoInicialSemana(),
                request.getPesoActual(),
                tendenciaPeso,
                request.getPromedioCaloriasSemana(),
                request.getPromedioAguaSemana(),
                request.getPromedioSuenoSemana(),
                request.getDiasEntrenadosSemana()
        );
    }
}
