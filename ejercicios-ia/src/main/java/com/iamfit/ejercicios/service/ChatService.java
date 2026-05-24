package com.iamfit.ejercicios.service;

import com.iamfit.ejercicios.dto.ChatRequestDto;
import com.iamfit.ejercicios.dto.ChatResponseDto;
import com.iamfit.ejercicios.exception.ChatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponseDto consultarEjercicio(ChatRequestDto pregunta) {
        String consulta = pregunta.pregunta();

        List<Document> documentosRelacionados = vectorStore.similaritySearch(
                SearchRequest.builder().query(consulta).topK(3).build()
        );

        if (documentosRelacionados == null) {
            documentosRelacionados = List.of();
        }


        String contexto = documentosRelacionados.stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .map(text -> text.replaceAll("\\s+", " ").trim())
                .collect(Collectors.joining("\n"));

        logger.info("Contexto limpio: {}", contexto);

        try {
            String respuestaIa = chatClient.prompt()
                    .system(s -> s.text(
                            "Eres un experto en fitness. Responde la pregunta del usuario " +
                                    "usando solo este contexto, puedes complementarlo con información tuya:\n\n"
                                    + contexto))
                    .user(consulta)
                    .call()
                    .content();

            return new ChatResponseDto(consulta, respuestaIa);

        } catch (Exception e) {
            logger.error("Error consultando a la IA", e);

            // Detecta rate limit (429) sin depender del SDK de Google
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("429")) {
                throw new ChatException("Límite de cuota alcanzado");
            }

            throw new ChatException("Error en la comunicación con la IA");
        }
    }
}