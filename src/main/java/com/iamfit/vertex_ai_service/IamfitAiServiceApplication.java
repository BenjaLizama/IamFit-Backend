package com.iamfit.vertex_ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IamfitAiServiceApplication {

    public static void main(String[] args) {
        String credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentials == null || credentials.isEmpty()) {
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS",
                    "C:/credenciales-ia/gcp-llave.json");
        }
        SpringApplication.run(IamfitAiServiceApplication.class, args);
    }
}