package com.iamfit.usuarios_perfiles.service;

import com.iamfit.usuarios_perfiles.dto.ProfileContextDTO;
import com.iamfit.usuarios_perfiles.dto.UserProfileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProfileContextServiceTest {

    private WebClient alimentacionClient;
    private WebClient ejerciciosClient;

    // Guardamos las referencias al primer eslabón de la cadena
    private WebClient.RequestHeadersUriSpec alimentacionUriSpec;
    private WebClient.RequestHeadersUriSpec ejerciciosUriSpec;

    private ProfileContextService service;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        WebClient.Builder builder = mock(WebClient.Builder.class);

        // 1. Instanciamos los clientes base
        alimentacionClient = mock(WebClient.class);
        ejerciciosClient = mock(WebClient.class);

        // 2. Instanciamos los mocks para la parte get()
        alimentacionUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        ejerciciosUriSpec = mock(WebClient.RequestHeadersUriSpec.class);

        // 3. Conectamos client.get() -> UriSpec
        when(alimentacionClient.get()).thenReturn(alimentacionUriSpec);
        when(ejerciciosClient.get()).thenReturn(ejerciciosUriSpec);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(alimentacionClient, ejerciciosClient);

        service = new ProfileContextService(builder, "http://alimentacion", "http://ejercicios");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubUri(WebClient.RequestHeadersUriSpec uriSpec, String uri, Map<String, Object> result) {
        // Creamos los mocks para el resto de la cadena
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        // Encadenamos explícitamente
        when(uriSpec.uri(uri)).thenReturn(headersSpec);
        // Usamos any() general para evitar problemas con los varargs de header()
        when(headersSpec.header(anyString(), any(String[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mono);
        when(mono.block()).thenReturn(result);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubUriThrows(WebClient.RequestHeadersUriSpec uriSpec, String uri) {
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        when(uriSpec.uri(uri)).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any(String[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mono);
        when(mono.block()).thenThrow(new RuntimeException("boom"));
    }

    @Test
    void buildContext_returnsAllSections() {
        // Pasamos el UriSpec correspondiente en lugar del Client entero
        stubUri(ejerciciosUriSpec, "/api/v1/routines/limits", Map.of("max", 5));
        stubUri(alimentacionUriSpec, "/api/v1/food/limits", Map.of("foodMax", 10));
        stubUri(alimentacionUriSpec, "/api/v1/food/meal-plans/active", Map.of("plan", "active"));
        stubUri(alimentacionUriSpec, "/api/v1/food/calories", Map.of("kcal", 2000));

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
        stubUriThrows(ejerciciosUriSpec, "/api/v1/routines/limits");
        stubUriThrows(alimentacionUriSpec, "/api/v1/food/limits");
        stubUriThrows(alimentacionUriSpec, "/api/v1/food/meal-plans/active");
        stubUriThrows(alimentacionUriSpec, "/api/v1/food/calories");

        ProfileContextDTO context = service.buildContext(
                UserProfileDTO.builder().build(), "token123");

        assertThat(context.getRoutineLimits()).isEmpty();
        assertThat(context.getFoodLimits()).isEmpty();
        assertThat(context.getActiveMealPlan()).isNull();
        assertThat(context.getTodayNutrition()).isEmpty();
    }
}
