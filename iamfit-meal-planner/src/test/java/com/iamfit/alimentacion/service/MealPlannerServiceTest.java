package com.iamfit.alimentacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamfit.alimentacion.dto.MealPlanResponse;
import com.iamfit.alimentacion.dto.UserPreferencesRequest;
import com.iamfit.alimentacion.exception.MealPlanGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlannerServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private MealPlannerService mealPlannerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generateMealPlan_returnsValidResponse() throws Exception {
        // Arrange
        String fakeJson = """
                {
                  "objetivo": "Ganar músculo",
                  "menu": {
                    "lunes":    { "desayuno": "Avena con claras", "almuerzo": "Pechuga con arroz", "cena": "Salmón con batata", "snacks": ["Proteína en polvo"] },
                    "martes":   { "desayuno": "Huevos revueltos", "almuerzo": "Atún con quinoa", "cena": "Pollo al horno", "snacks": ["Almendras"] },
                    "miercoles":{ "desayuno": "Yogur griego", "almuerzo": "Carne magra con verduras", "cena": "Omelette de claras", "snacks": ["Plátano"] },
                    "jueves":   { "desayuno": "Tostadas integrales con pavo", "almuerzo": "Pollo con pasta", "cena": "Merluza con ensalada", "snacks": ["Nueces"] },
                    "viernes":  { "desayuno": "Batido de proteína", "almuerzo": "Arroz con legumbres", "cena": "Ternera con espinacas", "snacks": ["Manzana"] },
                    "sabado":   { "desayuno": "Pancakes de avena", "almuerzo": "Sushi de atún", "cena": "Pollo a la plancha", "snacks": ["Requesón"] },
                    "domingo":  { "desayuno": "Tostadas con aguacate y huevo", "almuerzo": "Pavo al horno", "cena": "Ensalada de pollo", "snacks": ["Proteína en polvo"] }
                  },
                  "recomendaciones_nutricionales": "Consume al menos 2g de proteína por kg de peso corporal."
                }
                """;

        // Inline mock of fluent ChatClient chain
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(fakeJson);

        // Inject real ObjectMapper
        var field = MealPlannerService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(mealPlannerService, objectMapper);

        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setGoal("Ganar músculo");
        request.setPreferences(List.of("Alta proteína"));
        request.setAllergies(List.of("mariscos"));
        request.setLikes(List.of("pollo", "huevos"));
        request.setDislikes(List.of("brócoli"));

        // Act
        MealPlanResponse response = mealPlannerService.generateMealPlan(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getObjetivo()).isEqualTo("Ganar músculo");
        assertThat(response.getMenu()).containsKey("lunes");
        assertThat(response.getMenu().get("lunes").getDesayuno()).isEqualTo("Avena con claras");
        assertThat(response.getRecomendacionesNutricionales()).isNotBlank();
    }

    @Test
    void generateMealPlan_throwsWhenAiReturnsInvalidJson() throws Exception {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Este no es JSON válido");

        var field = MealPlannerService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(mealPlannerService, objectMapper);

        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setGoal("Bajar de peso");

        assertThatThrownBy(() -> mealPlannerService.generateMealPlan(request))
                .isInstanceOf(MealPlanGenerationException.class)
                .hasMessageContaining("JSON válido");
    }
}
