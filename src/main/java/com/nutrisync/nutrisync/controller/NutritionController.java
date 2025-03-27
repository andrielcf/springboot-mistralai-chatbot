package com.nutrisync.nutrisync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutrisync.nutrisync.dto.DietPlanResponse;
import com.nutrisync.nutrisync.dto.UserRequest;
import com.nutrisync.nutrisync.service.NutritionService;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {
    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<DietPlanResponse> generatePlan(@RequestBody UserRequest request) {
        DietPlanResponse response = nutritionService.generateDietPlan(request);
        return ResponseEntity.ok(response);
    }
}
