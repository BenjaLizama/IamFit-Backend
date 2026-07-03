package com.iamfit.usuarios_perfiles;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsuariosPerfilesApplicationTests {

	// Simula el JwtDecoder para que el contexto no falle intentando leer la llave pública
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
		// Si el test pasa, significa que el contexto arrancó correctamente
	}

}
