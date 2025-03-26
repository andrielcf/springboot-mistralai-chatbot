package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class Meal {
    private String uuid;
    private String name;
    private String mealType;
    private String localTime;
    private List<FoodItem> foodItems;
}
