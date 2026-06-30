package com.iamfit.alimentacion.service;

import com.iamfit.alimentacion.dto.FoodItemDto;
import com.iamfit.alimentacion.dto.FoodSearchResponse;
import com.iamfit.alimentacion.entity.FoodItem;
import com.iamfit.alimentacion.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodCatalogServiceTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private FoodCatalogService service;

    private FoodItem item;

    @BeforeEach
    void setUp() {
        item = new FoodItem();
        item.setId(UUID.randomUUID());
        item.setName("Pollo");
        item.setNameEn("Chicken");
        item.setCalories(165.0);
        item.setProtein(31.0);
        item.setCarbohydrates(0.0);
        item.setFat(3.6);
        item.setFiber(0.0);
        item.setFoodCategory("carnes");
        item.setServingSizeG(100.0);
    }

    @Test
    void searchLocal_mapsResults() {
        when(foodItemRepository.searchByNameOrNameEn("pollo")).thenReturn(List.of(item));

        List<FoodItemDto> result = service.searchLocal("pollo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Pollo");
        assertThat(result.get(0).getProtein()).isEqualTo(31.0);
    }

    @Test
    void search_withEnoughLocalResults_skipsExternal() {
        when(foodItemRepository.searchByNameOrNameEn("pollo"))
                .thenReturn(List.of(item, item, item));

        FoodSearchResponse response = service.search("pollo");

        assertThat(response.getLocalResults()).hasSize(3);
        assertThat(response.isExternalSearchPerformed()).isFalse();
        verify(webClientBuilder, never()).build();
    }

    @Test
    void search_withFewLocalResults_andNoApiKey_skipsExternal() {
        when(foodItemRepository.searchByNameOrNameEn("pollo")).thenReturn(List.of(item));

        FoodSearchResponse response = service.search("pollo");

        assertThat(response.isExternalSearchPerformed()).isFalse();
        verify(webClientBuilder, never()).build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void search_withFewLocalResults_andApiKey_performsExternalAndSaves() {
        ReflectionTestUtils.setField(service, "calorieNinjasApiKey", "secret");
        ReflectionTestUtils.setField(service, "calorieNinjasBaseUrl", "http://api");

        when(foodItemRepository.searchByNameOrNameEn("pollo")).thenReturn(List.of());

        // Desarmamos la cadena del WebClient para evitar el NPE
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        // Usamos any(String[].class) para el vararg
        when(headersSpec.header(anyString(), any(String[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mono);

        Map<String, Object> external = Map.of("items", List.of(Map.of(
                "name", "egg",
                "calories", 155,
                "protein_g", 13.0,
                "carbohydrates_total_g", 1.1,
                "fat_total_g", 11.0)));

        when(mono.block()).thenReturn(external);
        when(foodItemRepository.existsByExternalId(anyString())).thenReturn(false);

        FoodSearchResponse response = service.search("pollo");

        assertThat(response.isExternalSearchPerformed()).isTrue();
        assertThat(response.getExternalResults()).hasSize(1);
        assertThat(response.getExternalResults().get(0).getName()).isEqualTo("egg");
        verify(foodItemRepository).save(any(FoodItem.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void search_externalApiError_returnsEmptyExternal() {
        ReflectionTestUtils.setField(service, "calorieNinjasApiKey", "secret");
        ReflectionTestUtils.setField(service, "calorieNinjasBaseUrl", "http://api");

        when(foodItemRepository.searchByNameOrNameEn("pollo")).thenReturn(List.of());

        // Mismos mocks explícitos para el test de error
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any(String[].class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mono);

        when(mono.block()).thenThrow(new RuntimeException("boom"));

        FoodSearchResponse response = service.search("pollo");

        assertThat(response.isExternalSearchPerformed()).isTrue();
        assertThat(response.getExternalResults()).isEmpty();
    }

    @Test
    void getById_returnsDto() {
        when(foodItemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        FoodItemDto dto = service.getById(item.getId());

        assertThat(dto.getName()).isEqualTo("Pollo");
    }

    @Test
    void getById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(foodItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alimento no encontrado");
    }

    @Test
    void getByCategory_mapsResults() {
        when(foodItemRepository.findByFoodCategory("carnes")).thenReturn(List.of(item));

        List<FoodItemDto> result = service.getByCategory("carnes");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFoodCategory()).isEqualTo("carnes");
    }
}
