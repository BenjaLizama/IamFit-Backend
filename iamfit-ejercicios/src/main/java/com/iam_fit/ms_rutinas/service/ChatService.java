package com.iam_fit.ms_rutinas.service;

import com.google.genai.errors.ClientException;
import com.iam_fit.ms_rutinas.dto.ChatRequestDto;
import com.iam_fit.ms_rutinas.dto.ChatResponseDto;
import com.iam_fit.ms_rutinas.exception.ChatException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    private static final Logger logger =
            LoggerFactory.getLogger(ChatService.class);

    public ChatResponseDto consultarEjercicio(ChatRequestDto pregunta) {
        String consulta = pregunta.pregunta();

        List<Document> documentosRelacionados = vectorStore.similaritySearch(
                SearchRequest.builder().query(consulta).topK(3).build()
        );
        String contexto = documentosRelacionados.stream()
                .map(Document::getText).filter(Objects::nonNull)
                .map(text -> text.replaceAll("\\s+", " ").trim())
                .collect(Collectors.joining("\n"));
    logger.info("contexto limpio: {}", contexto);


    try {

        String respuestaIa =
                chatClient.prompt()
                        .system(s -> s.text("Eres un experto en fitness. Responde la pregunta del usuario usando solo este contexto puedes complementarlo con informacion tuya:\n\n" + contexto))
                        .user(consulta)
                        .call()
                        .content();

        return new ChatResponseDto(consulta,respuestaIa);

    }catch (ClientException e){

        logger.error("Error en la comunicacion con gemini", e);
        String mensaje = e.getMessage();

        if(mensaje !=null && mensaje.contains("429")){
            throw new ChatException("limite de cuota alcanzado");
        }
        throw new ChatException("Error en la comunicacion con gemini");


    }catch (Exception e){
        logger.error("Error consultando a gemini", e);

        throw new ChatException("error consultando a la IA");

    }



    }
}