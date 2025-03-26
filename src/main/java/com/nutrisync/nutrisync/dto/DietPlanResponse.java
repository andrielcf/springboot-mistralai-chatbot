package com.nutrisync.nutrisync.dto;

import lombok.Data;

@Data
public class DietPlanResponse {
    private String message;
    private DietPlan dietPlan;
}
