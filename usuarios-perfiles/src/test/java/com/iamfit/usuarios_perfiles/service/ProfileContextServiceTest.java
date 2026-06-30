package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProfileContextServiceTest {

    private WebClient alimentacionClient;
    private WebClient ejerciciosClient;
    private ProfileContextService service;

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        alimentacionClient = mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);
        ejerciciosClient = mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        // First build() -> alimentacion, second build() -> ejercicios (constructor order)
        when(builder.build()).thenReturn(alimentacionClient, ejerciciosClient);

        service = new ProfileContextService(builder, "http://alimentacion", "http://ejercicios");
    }

    @SuppressWarnings("unchecked")
    private void stubUri(WebClient client, String uri, Map<String, Object> result) {
        when(client.get()
                .uri(eq(uri))
                .header(eq("Authorization"), anyString())
                .retrieve()
                .bodyToMono(Map.class)
                .block()).thenReturn((Map) result);
    }

    @SuppressWarnings("unchecked")
    private void stubUriThrows(WebClient client, String uri) {
        when(client.get()
                .uri(eq(uri))
                .header(eq("Authorization"), anyString())
                .retrieve()
                .bodyToMono(Map.class)
                .block()).thenThrow(new RuntimeException("boom"));
    }

    @Test
    void buildContext_returnsAllSections() {
        stubUri(ejerciciosClient, "/api/v1/routines/limits", Map.of("max", 5));
        stubUri(alimentacionClient, "/api/v1/food/limits", Map.of("foodMax", 10));
        stubUri(alimentacionClient, "/api/v1/food/meal-plans/active", Map.of("plan", "active"));
        stubUri(alimentacionClient, "/api/v1/food/calories", Map.of("kcal", 2000));

        UserProfileDTO profile = UserProfileDTO.builder().nickname("Benja").build();

        ProfileContextDTO context = service.buildContext(profile, "token123");

        assertThat(context.getProfile()).isEqualTo(profile);
        assertThat(context.getRoutineLimits()).containsEntry("max", 5);
        assertThat(context.getFoodLimits()).containsEntry("foodMax", 10);
        assertThat(context.getActiveMealPlan()).containsEntry("plan", "active");
        assertThat(context.getTodayNutrition()).containsEntry("kcal", 2000);
    }

    @Test
    void buildContext_handlesErrorsWithFallbacks() {
        stubUriThrows(ejerciciosClient, "/api/v1/routines/limits");
        stubUriThrows(alimentacionClient, "/api/v1/food/limits");
        stubUriThrows(alimentacionClient, "/api/v1/food/meal-plans/active");
        stubUriThrows(alimentacionClient, "/api/v1/food/calories");

        ProfileContextDTO context = service.buildContext(
                UserProfileDTO.builder().build(), "token123");

        assertThat(context.getRoutineLimits()).isEmpty();
        assertThat(context.getFoodLimits()).isEmpty();
        assertThat(context.getActiveMealPlan()).isNull();
        assertThat(context.getTodayNutrition()).isEmpty();
    }
}
