package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DietPlanResponse {
    private String message;
    private List<DayPlan> weeklyPlan;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DayPlan {
        private int day;
        private List<Meal> planDay;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Meal {
        private String meal;
        private String time;
        private String description;
    }
}
