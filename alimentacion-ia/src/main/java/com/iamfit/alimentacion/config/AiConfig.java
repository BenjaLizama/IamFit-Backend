package com.iamfit.alimentacion.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    // ChatClient se construye en MealPlannerService con el Builder.
    // Esta clase queda disponible para futura configuración (interceptors, etc.)
}