package com.iamfit.ejercicios.controller;

import com.iamfit.ejercicios.dto.ChatRequestDto;
import com.iamfit.ejercicios.dto.ChatResponseDto;
import com.iamfit.ejercicios.service.ChatService;
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
