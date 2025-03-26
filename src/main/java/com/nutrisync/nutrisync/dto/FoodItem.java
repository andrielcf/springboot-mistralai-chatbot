package com.nutrisync.nutrisync.dto;

import lombok.Data;

@Data
public class FoodItem {
    private String name;
    private double grams;
    private double calories;
    private double protein;
    private double carbohydrates;
    private double fats;
}
