package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class DietPlan {
    private String uuid;
    private List<Meal> meals;
}
