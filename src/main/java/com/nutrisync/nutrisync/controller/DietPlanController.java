package com.nutrisync.nutrisync.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.text.DocumentException;
import com.nutrisync.nutrisync.dto.DietPlanResponse;
import com.nutrisync.nutrisync.dto.UserRequest;
import com.nutrisync.nutrisync.entity.DietPlan;
import com.nutrisync.nutrisync.respository.DietPlanRepository;
import com.nutrisync.nutrisync.service.NutritionService;
import com.nutrisync.nutrisync.service.PdfGenerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {
    
    private final NutritionService nutritionService;
    private final DietPlanRepository dietPlanRepository;
    private final PdfGenerationService pdfGenerationService;
    
    @PostMapping
    public ResponseEntity<DietPlanResponse> generateDietPlan(
            @RequestBody UserRequest userRequest,
            @RequestHeader("X-User-ID") Long userId) {
        
        DietPlanResponse response = nutritionService.generateDietPlan(userRequest, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{planId}/pdf")
    public ResponseEntity<byte[]> getDietPlanPdf(@PathVariable Long planId) {
        DietPlan dietPlan = dietPlanRepository.findById(planId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado"));
        
        try {
            byte[] pdfBytes = pdfGenerationService.generateDietPlanPdf(dietPlan);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("plano-alimentar.pdf").build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF");
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DietPlan>> getUserDietPlans(@PathVariable Long userId) {
        List<DietPlan> plans = dietPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(plans);
    }
}
