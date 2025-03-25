package com.nutrisync.nutrisync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MistralAiConfig {
    @Value("${mistralai.api.key}")
    private String apiKey;
    
    @Bean
    public RestTemplate mistralRestTemplate() {
        return new RestTemplate();
    }
    
    public String getApiKey() {
        return apiKey;
    }
}
