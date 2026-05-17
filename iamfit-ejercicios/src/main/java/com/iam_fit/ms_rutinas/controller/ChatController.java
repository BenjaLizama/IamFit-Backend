package com.iam_fit.ms_rutinas.controller;

import com.iam_fit.ms_rutinas.dto.ChatRequestDto;
import com.iam_fit.ms_rutinas.dto.ChatResponseDto;
import com.iam_fit.ms_rutinas.service.ChatService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/preguntar")
    public ChatResponseDto preguntar(@Valid @RequestBody ChatRequestDto pregunta){
            return chatService.consultarEjercicio(pregunta);
    }
}
