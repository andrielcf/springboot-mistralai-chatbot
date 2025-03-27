package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class DietPlanResponse {
    private String message;
    private List<DayPlan> weeklyPlan;

    @Data
    public static class DayPlan {
        private int day;
        private List<Meal> planDay;
    }

    @Data
    public static class Meal {
        private String meal;
        private String time;
        private String description;
    }
}
