package com.nutrisync.nutrisync.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import com.nutrisync.nutrisync.dto.UserRequest;

@Service
public class NutritionService {
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";
    private static final String MODEL = "mistral-tiny";
    private static final double TEMPERATURE = 0.3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mistralai.api.key}")
    private String apiKey;

    public NutritionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // public DietPlanResponse generateDietPlan(UserRequest userRequest) {
    //     try {
    //         String prompt = buildPrompt(userRequest);
    //         String aiResponse = getAiResponse(prompt);
    //         return parseResponse(aiResponse);
    //     } catch (Exception e) {
    //         throw new NutritionServiceException("Failed to generate diet plan: " + e.getMessage(), e);
    //     }
    // }
    private void validateMealTimes(DietPlanResponse response) {
        if (response == null || response.getWeeklyPlan() == null) return;
        
        for (DietPlanResponse.DayPlan day : response.getWeeklyPlan()) {
            if (day.getPlanDay() != null) {
                for (DietPlanResponse.Meal meal : day.getPlanDay()) {
                    if (meal.getTime() == null || !meal.getTime().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                        throw new NutritionServiceException("Formato de horário inválido para refeição: " + meal.getMeal());
                    }
                }
            }
        }
    }

    public DietPlanResponse generateDietPlan(UserRequest userRequest) {
        try {
            String prompt = buildPrompt(userRequest);
            String aiResponse = getAiResponse(prompt);
            DietPlanResponse response = parseResponse(aiResponse);
            validateMealTimes(response); // Valida os horários
            return response;
        } catch (Exception e) {
            throw new NutritionServiceException("Failed to generate diet plan: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(UserRequest user) {
        return String.format("""
            Gere um plano alimentar SEMANAL em JSON com EXATAMENTE esta estrutura:
            
            {
              "message": "descrição do plano alimentar",
              "weeklyPlan": [
                {
                  "day": 0,
                  "planDay": [
                    {
                      "meal": "Nome da refeição"
                      "time": "HH:MM",                  // Ex: "08:00" (formato 24h)
                      "description": "Descrição COM QUANTIDADES e UNIDADES PADRONIZADAS" // Ex: "Ovos (2 unidades), pão integral (2 fatias)"
                    }
                  ]
                }
              ]
            }
            
            REQUISITOS OBRIGATÓRIOS:
            1. Resposta em Português SEMPRE
            2. 7 dias completos (day 0 a day 6)
            3. Incluir horário para cada refeição no formato "HH:MM" (24h)
            4. Objetivo: %s
            5. Restrições: %s
            6. Dados: %d anos, %.2fm, %.2fkg
            7. Pedido específico: %s
            
            AVISOS IMPORTANTES:
            - Se o pedido do usuário for prejudicial à saúde, inclua um alerta na mensagem
            - Horários devem ser realistas e espaçados adequadamente
            - Retorne SOMENTE o JSON válido, SEM texto adicional ou markdown
            """,
            user.getGoal(),
            user.getRestrictions(),
            user.getAge(),
            user.getHeight(),
            user.getWeight(),
            user.getPrompdousuario()
        );
    }

    private String getAiResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> request = Map.of(
            "model", MODEL,
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "response_format", Map.of("type", "json_object"),
            "temperature", TEMPERATURE
        );

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                MISTRAL_API_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            return extractContentFromResponse(response.getBody());
        } catch (Exception e) {
            throw new NutritionServiceException("Failed to get AI response", e);
        }
    }

    private String extractContentFromResponse(Map<String, Object> responseBody) {
        try {
            if (responseBody == null || !responseBody.containsKey("choices")) {
                throw new NutritionServiceException("Invalid API response format");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");
        } catch (ClassCastException | NullPointerException e) {
            throw new NutritionServiceException("Failed to parse API response", e);
        }
    }

    private DietPlanResponse parseResponse(String json) throws JsonProcessingException {
        try {
            return objectMapper.readValue(json, DietPlanResponse.class);
        } catch (JsonProcessingException e) {
            throw new NutritionServiceException("Failed to parse JSON response", e);
        }
    }

    public static class NutritionServiceException extends RuntimeException {
        public NutritionServiceException(String message) {
            super(message);
        }
        public NutritionServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
