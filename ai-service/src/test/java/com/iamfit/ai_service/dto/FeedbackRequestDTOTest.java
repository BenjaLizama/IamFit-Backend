package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FeedbackRequestDTOTest {

    @Test
    @DisplayName("FeedbackRequestDTO - Debe funcionar correctamente con Getter, Setter y ToString")
    void testFeedbackRequestDTO() {
        FeedbackRequestDTO dto = new FeedbackRequestDTO();
        dto.setUserId("USER123");
        dto.setPesoActual(75.5);
        dto.setCaloriasConsumidas(2500);
        dto.setAguaConsumida(3.0);
        dto.setHorasSueno(8.0);
        dto.setEjerciciosRealizados(List.of("Sentadilla"));
        dto.setPesoInicialSemana(76.0);
        dto.setPromedioCaloriasSemana(2400);
        dto.setPromedioAguaSemana(2.5);
        dto.setPromedioSuenoSemana(7.5);
        dto.setDiasEntrenadosSemana(4);
        dto.setObjetivo("mantener");

        assertEquals("USER123", dto.getUserId());
        assertEquals(75.5, dto.getPesoActual());
        assertEquals(2500, dto.getCaloriasConsumidas());
        assertEquals(3.0, dto.getAguaConsumida());
        assertEquals(8.0, dto.getHorasSueno());
        assertEquals("Sentadilla", dto.getEjerciciosRealizados().get(0));
        assertEquals(76.0, dto.getPesoInicialSemana());
        assertEquals(2400, dto.getPromedioCaloriasSemana());
        assertEquals(2.5, dto.getPromedioAguaSemana());
        assertEquals(7.5, dto.getPromedioSuenoSemana());
        assertEquals(4, dto.getDiasEntrenadosSemana());
        assertEquals("mantener", dto.getObjetivo());
        assertNotNull(dto.toString());
    }
}