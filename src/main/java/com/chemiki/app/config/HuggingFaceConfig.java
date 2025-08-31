package com.chemiki.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HuggingFaceConfig {

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${huggingface.model.name:abishektiwari/Nepali-Bert-offensiveDetection}")
    private String modelName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getModelName() {
        return modelName;
    }
}
