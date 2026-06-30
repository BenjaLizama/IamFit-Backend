package com.iamfit.ejercicios;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration," +
				"org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatAutoConfiguration"
})
class MsRutinasApplicationTests {

	@MockitoBean
	private JwtDecoder jwtDecoder;
	@MockitoBean
	private ChatClient.Builder chatClient;

	@Test
	void contextLoads() {
		// Ahora el contexto cargará feliz porque RoutineService recibirá el mock de su dependencia número 5
	}

}