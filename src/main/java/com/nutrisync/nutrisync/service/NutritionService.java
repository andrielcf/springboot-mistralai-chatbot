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
import com.nutrisync.nutrisync.entity.DayPlan;
import com.nutrisync.nutrisync.entity.DietPlan;
import com.nutrisync.nutrisync.entity.Meal;
import com.nutrisync.nutrisync.respository.DietPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NutritionService {
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";
    private static final String MODEL = "mistral-tiny";
    private static final double TEMPERATURE = 0.3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DietPlanRepository dietPlanRepository;

    @Value("${mistralai.api.key}")
    private String apiKey;

    public DietPlanResponse generateDietPlan(UserRequest userRequest, Long userId) {
        try {
            String prompt = buildPrompt(userRequest);
            String aiResponse = getAiResponse(prompt);
            DietPlanResponse response = parseResponse(aiResponse);
            validateMealTimes(response);
            saveDietPlan(response, userId);
            return response;
        } catch (Exception e) {
            throw new NutritionServiceException("Failed to generate diet plan", e);
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
                          "meal": "Nome da refeição",
                          "time": "HH:MM",
                          "description": "Descrição COM QUANTIDADES e UNIDADES PADRONIZADAS"
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
                - Quantidades devem ser precisas (ex: 200g, 2 unidades)
                - Variedade de alimentos ao longo da semana
                - Retorne SOMENTE o JSON válido, SEM texto adicional ou markdown
                """,
                user.getGoal(),
                user.getRestrictions(),
                user.getAge(),
                user.getHeight(),
                user.getWeight(),
                user.getPromptUsuario());
    }

    private String getAiResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object"),
                "temperature", TEMPERATURE);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    MISTRAL_API_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    new ParameterizedTypeReference<>() {
                    });

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

    private void validateMealTimes(DietPlanResponse response) {
        if (response == null || response.getWeeklyPlan() == null) {
            throw new NutritionServiceException("Resposta inválida da API: plano semanal ausente");
        }

        if (response.getWeeklyPlan().size() != 7) {
            throw new NutritionServiceException("O plano deve conter exatamente 7 dias");
        }

        for (DietPlanResponse.DayPlan day : response.getWeeklyPlan()) {
            if (day.getPlanDay() == null || day.getPlanDay().isEmpty()) {
                throw new NutritionServiceException("Dia " + day.getDay() + " sem refeições definidas");
            }

            for (DietPlanResponse.Meal meal : day.getPlanDay()) {
                if (meal.getTime() == null || !meal.getTime().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    throw new NutritionServiceException("Formato de horário inválido para refeição: " + meal.getMeal());
                }

                if (meal.getDescription() == null || meal.getDescription().trim().isEmpty()) {
                    throw new NutritionServiceException("Descrição ausente para a refeição: " + meal.getMeal());
                }
            }
        }
    }

    private void saveDietPlan(DietPlanResponse response, Long userId) {
        DietPlan dietPlan = new DietPlan();
        dietPlan.setUserId(userId);
        dietPlan.setMessage(response.getMessage());

        for (DietPlanResponse.DayPlan dayPlanDto : response.getWeeklyPlan()) {
            DayPlan dayPlan = new DayPlan();
            dayPlan.setDietPlan(dietPlan);
            dayPlan.setDay(dayPlanDto.getDay());

            for (DietPlanResponse.Meal mealDto : dayPlanDto.getPlanDay()) {
                Meal meal = new Meal();
                meal.setDayPlan(dayPlan);
                meal.setMealName(mealDto.getMeal());
                meal.setTime(mealDto.getTime());
                meal.setDescription(mealDto.getDescription());
                dayPlan.getPlanDay().add(meal);
            }

            dietPlan.getWeeklyPlan().add(dayPlan);
        }

        dietPlanRepository.save(dietPlan);
    }

    public class NutritionServiceException extends RuntimeException {
        public NutritionServiceException(String message) {
            super(message);
        }

        public NutritionServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ... outros métodos (getAiResponse, parseResponse, validateMealTimes,
    // saveDietPlan) ...
}
