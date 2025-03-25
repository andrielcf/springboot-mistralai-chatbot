package com.nutrisync.nutrisync.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutrisync.nutrisync.service.MistralAiService;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {
    
    private final MistralAiService mistralAiService;

    @PostMapping
    public String chat(@RequestBody String userMessage) {
        return mistralAiService.generateResponse(userMessage);
    }

    @PostMapping("/conversation")
    public String conversation(@RequestBody ConversationRequest request) {
        return mistralAiService.generateResponse(request.getMessage());
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Chatbot API está online e funcionando!";
    }

    @Data
    static class ConversationRequest {
        private String message;
    }
}
