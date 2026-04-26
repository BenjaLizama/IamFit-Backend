package com.iamfit.usuarios_perfiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class UsuariosPerfilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosPerfilesApplication.class, args);
	}

}
