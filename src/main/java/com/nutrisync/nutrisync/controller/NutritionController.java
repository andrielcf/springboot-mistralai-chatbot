package com.nutrisync.nutrisync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutrisync.nutrisync.dto.DietPlanResponse;
import com.nutrisync.nutrisync.dto.UserProfile;
import com.nutrisync.nutrisync.service.NutritionPlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {
    private final NutritionPlanService nutritionPlanService;

    @PostMapping("/generate-plan")
    public ResponseEntity<DietPlanResponse> generatePlan(@RequestBody UserProfile userProfile) {
        DietPlanResponse response = nutritionPlanService.generateDietPlan(userProfile);
        return ResponseEntity.ok(response);
    }
}
