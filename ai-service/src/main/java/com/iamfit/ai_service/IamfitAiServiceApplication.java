package com.iamfit.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@SpringBootApplication
public class IamfitAiServiceApplication {

    public static void main(String[] args) {
        loadGcpCredentials();
        SpringApplication.run(IamfitAiServiceApplication.class, args);
    }

    private static void loadGcpCredentials() {
        String base64 = System.getenv("GCP_CREDENTIALS");
        if (base64 == null || base64.isBlank()) {
            // Desarrollo local sin Docker — usa el .env local
            return;
        }
        try {
            // Decodifica el JSON y lo escribe en un archivo temporal
            byte[] decoded = Base64.getDecoder().decode(base64);
            Path tempFile = Files.createTempFile("gcp-credentials", ".json");
            Files.write(tempFile, decoded);
            tempFile.toFile().deleteOnExit();
            // Setea la variable que Google SDK busca
            System.setProperty(
                    "GOOGLE_APPLICATION_CREDENTIALS",
                    tempFile.toAbsolutePath().toString()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Error cargando credenciales GCP", e);
        }
    }
}