package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private double temperature;
}
