package com.nutrisync.nutrisync.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private int index;
        private Message message;
        private String finish_reason;
    }
}
