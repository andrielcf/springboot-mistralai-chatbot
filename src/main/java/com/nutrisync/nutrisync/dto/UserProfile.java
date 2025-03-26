package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserProfile {
    private int age;
    private double height;
    private double weight;
    private String goal;
    private List<String> restrictions;
}
