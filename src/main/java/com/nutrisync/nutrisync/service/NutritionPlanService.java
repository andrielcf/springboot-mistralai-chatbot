package com.nutrisync.nutrisync.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrisync.nutrisync.dto.DietPlanResponse;
import com.nutrisync.nutrisync.dto.UserProfile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NutritionPlanService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mistralai.api.key}")
    private String apiKey;

    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";

    public DietPlanResponse generateDietPlan(UserProfile userProfile) {
        String prompt = buildPrompt(userProfile);
        String aiResponse = getAiResponse(prompt);
        return parseResponse(aiResponse);
    }

    private String buildPrompt(UserProfile userProfile) {
        return """
                Gere um plano alimentar em JSON estruturado com as seguintes características:
    
                Requisitos do usuário:
                - Objetivo: %s
                - Restrições: %s
                - Características: %d anos, %.2f m, %.2f kg
                - Pedido específico do usuário: %s
    
                Estrutura exigida:
                {
                  "message": "Descreva o que a pessoa tem que saber sobre a dieta dela, em português",
                  "dietPlan": {
                    "meals": [
                      {
                        "name": "nome da refeição",
                        "mealType": "CAFE_DA_MANHA|ALMOCO|JANTAR|LANCHE",
                        "localTime": "HH:MM",
                        "foodItems": [
                          {
                            "name": "nome do alimento",
                            "grams": 0.0,
                            "calories": 0.0,
                            "protein": 0.0,
                            "carbohydrates": 0.0,
                            "fats": 0.0
                          }
                        ]
                      }
                    ]
                  }
                }
    
                NÃO INCLUA NENHUM TEXTO FORA DO JSON. NÃO USE MARKDOWN. APENAS O JSON.
                """.formatted(
                userProfile.getGoal(),
                userProfile.getRestrictions(),
                userProfile.getAge(),
                userProfile.getHeight(),
                userProfile.getWeight(),
                userProfile.getPrompdousuario()
        );
    }

    private String getAiResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mistral-tiny");
        requestBody.put("messages", new Object[] {
                Map.of("role", "user", "content", prompt)
        });
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                MISTRAL_API_URL,
                HttpMethod.POST,
                entity,
                Map.class);

        // Verifica se a resposta tem o formato esperado
        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");

                if (message != null && message.containsKey("content")) {
                    return message.get("content").toString();
                }
            }
        }

        throw new RuntimeException("Resposta da API inesperada ou inválida");
    }

    private DietPlanResponse parseResponse(String jsonResponse) {
        try {
            DietPlanResponse response = objectMapper.readValue(jsonResponse, DietPlanResponse.class);
            generateMissingUuids(response);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private void generateMissingUuids(DietPlanResponse response) {
        if (response.getDietPlan() != null) {
            if (response.getDietPlan().getUuid() == null) {
                response.getDietPlan().setUuid(UUID.randomUUID().toString());
            }

            if (response.getDietPlan().getMeals() != null) {
                response.getDietPlan().getMeals().forEach(meal -> {
                    if (meal.getUuid() == null) {
                        meal.setUuid(UUID.randomUUID().toString());
                    }
                });
            }
        }
    }
}
