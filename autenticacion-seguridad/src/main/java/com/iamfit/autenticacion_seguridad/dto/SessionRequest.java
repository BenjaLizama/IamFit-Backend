package com.iamfit.autenticacion_seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SessionRequest(

   @NotBlank(message = "El identificador del dispositio es obligarorio.")
   @Size(max = 255, message = "El identificador del dispositivo no puede superrar los 255 caracteres")
   String deviceId,

   @NotBlank(message = "El nombre del dispositivo es obligatorio.")
   @Size(max = 100, message = "El nombre del disposit")
   String deviceName

) {}
