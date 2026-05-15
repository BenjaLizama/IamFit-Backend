package com.iamfit.usuarios_perfiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.grpc.server.autoconfigure.security.GrpcSecurityAutoConfiguration;
import org.springframework.boot.grpc.server.autoconfigure.security.OAuth2ResourceServerAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = {
        GrpcSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class UsuariosPerfilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosPerfilesApplication.class, args);
    }
}
