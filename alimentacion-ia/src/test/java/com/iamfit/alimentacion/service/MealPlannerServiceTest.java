package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.client.UserProfileGrpcClient;
import com.iamfit.alimentacion.dto.MealPlanResponse;
import com.iamfit.alimentacion.dto.UserPreferencesRequest;
import com.iamfit.alimentacion.exception.MealPlanGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private UserProfileGrpcClient userProfileGrpcClient;

    private UserPreferencesRequest buildRequest() {
        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setGoal("Ganar músculo");
        request.setPreferences(List.of("Alta proteína"));
        request.setAllergies(List.of("mariscos"));
        request.setLikes(List.of("pollo", "huevos"));
        request.setDislikes(List.of("brócoli"));
        return request;
    }

    private void mockChatChain(String response) {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(response);
    }

    private MealPlannerService buildService() {
        return new MealPlannerService(chatClientBuilder, userProfileGrpcClient);
    }

    @Test
    void generateMealPlan_returnsValidResponse() {
        String fakeJson = """
                {
                  "objetivo": "Ganar músculo",
                  "menu": {
                    "lunes":     { "desayuno": "Avena con claras", "almuerzo": "Pechuga con arroz", "cena": "Salmón con batata", "snacks": ["Proteína en polvo"] },
                    "martes":    { "desayuno": "Huevos revueltos", "almuerzo": "Atún con quinoa", "cena": "Pollo al horno", "snacks": ["Almendras"] },
                    "miercoles": { "desayuno": "Yogur griego", "almuerzo": "Carne magra", "cena": "Omelette", "snacks": ["Plátano"] },
                    "jueves":    { "desayuno": "Tostadas con pavo", "almuerzo": "Pollo con pasta", "cena": "Merluza", "snacks": ["Nueces"] },
                    "viernes":   { "desayuno": "Batido proteína", "almuerzo": "Arroz con legumbres", "cena": "Ternera", "snacks": ["Manzana"] },
                    "sabado":    { "desayuno": "Pancakes avena", "almuerzo": "Sushi atún", "cena": "Pollo plancha", "snacks": ["Requesón"] },
                    "domingo":   { "desayuno": "Tostadas aguacate", "almuerzo": "Pavo al horno", "cena": "Ensalada pollo", "snacks": ["Proteína en polvo"] }
                  },
                  "recomendaciones_nutricionales": "Consume al menos 2g de proteína por kg de peso corporal."
                }
                """;

        mockChatChain(fakeJson);

        MealPlanResponse response = buildService().generateMealPlan(buildRequest());

        assertThat(response).isNotNull();
        assertThat(response.getObjetivo()).isEqualTo("Ganar músculo");
        assertThat(response.getMenu()).isNotNull();
        assertThat(response.getMenu().getLunes()).isNotNull();
        assertThat(response.getMenu().getLunes().getDesayuno()).isEqualTo("Avena con claras");
        assertThat(response.getRecomendacionesNutricionales()).isNotBlank();
    }

    @Test
    void generateMealPlan_throwsWhenAiReturnsInvalidJson() {
        mockChatChain("Esto no es JSON válido");

        assertThatThrownBy(() -> buildService().generateMealPlan(buildRequest()))
                .isInstanceOf(MealPlanGenerationException.class);
    }

    @Test
    void generateMealPlan_handlesEmptyPreferences() {
        String fakeJson = """
                {
                  "objetivo": "Bajar de peso",
                  "menu": {
                    "lunes":     { "desayuno": "Fruta", "almuerzo": "Ensalada", "cena": "Sopa", "snacks": [] },
                    "martes":    { "desayuno": "Yogur", "almuerzo": "Pollo", "cena": "Verduras", "snacks": [] },
                    "miercoles": { "desayuno": "Avena", "almuerzo": "Atún", "cena": "Tortilla", "snacks": [] },
                    "jueves":    { "desayuno": "Fruta", "almuerzo": "Ensalada", "cena": "Sopa", "snacks": [] },
                    "viernes":   { "desayuno": "Yogur", "almuerzo": "Pollo", "cena": "Verduras", "snacks": [] },
                    "sabado":    { "desayuno": "Avena", "almuerzo": "Atún", "cena": "Tortilla", "snacks": [] },
                    "domingo":   { "desayuno": "Fruta", "almuerzo": "Ensalada", "cena": "Sopa", "snacks": [] }
                  },
                  "recomendaciones_nutricionales": "Mantén un déficit calórico moderado."
                }
                """;

        mockChatChain(fakeJson);

        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setGoal("Bajar de peso");

        MealPlanResponse response = buildService().generateMealPlan(request);

        assertThat(response).isNotNull();
        assertThat(response.getMenu().getLunes()).isNotNull();
    }
}