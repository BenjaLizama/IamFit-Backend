package com.iamfit.ai_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WellbeingCheckInRequestDTO {

    @Min(value = 1, message = "El estado de ánimo debe ser entre 1 y 5")
    @Max(value = 5, message = "El estado de ánimo debe ser entre 1 y 5")
    private int estadoAnimo;        // 1=muy mal, 2=mal, 3=regular, 4=bien, 5=excelente

    @Min(value = 1, message = "El nivel de estrés debe ser entre 1 y 5")
    @Max(value = 5, message = "El nivel de estrés debe ser entre 1 y 5")
    private int nivelEstres;        // 1=sin estrés, 5=muy estresado

    @Size(max = 500, message = "La nota no puede superar los 500 caracteres")
    private String nota;            // opcional, texto libre del usuario
}