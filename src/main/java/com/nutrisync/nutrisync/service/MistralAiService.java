package com.nutrisync.nutrisync.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nutrisync.nutrisync.config.MistralAiConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MistralAiService {
    
    private final RestTemplate restTemplate;
    private final MistralAiConfig config;
    
    public String generateResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", "mistral-tiny");
        request.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            "https://api.mistral.ai/v1/chat/completions",
            HttpMethod.POST,
            entity,
            Map.class);
            
        // Processar resposta
        return extractResponse(response.getBody());
    }
    
    private String extractResponse(Map<String, Object> response) {
        return response.toString();
    }
}
