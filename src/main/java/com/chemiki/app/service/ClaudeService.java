package com.chemiki.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaudeService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    private final WebClient.Builder webClientBuilder;

    public boolean isHateSpeech(String commentText) {
        String prompt = "Determine if the following comment contains hate speech specifically targeting the Bahun community in Nepal. " +
                "Reply with 'YES' if the comment is offensive or derogatory toward the Bahun community. " +
                "Reply with 'NO' for all comments about other communities (positive or negative) or if the comment is not offensive toward the Bahun community. " +
                "Return only 'YES' or 'NO'.\nComment: \"" + commentText + "\"";

        Map<String, Object> body = Map.of(
                "model", "claude-3-haiku-20240307",
                "max_tokens", 50,
                "temperature", 0.3,
                "messages", new Object[] {
                        Map.of("role", "user", "content", prompt)
                }
        );

        try {
            Map response = webClientBuilder.build() // Build the WebClient instance
                    .post()
                    .uri(apiUrl)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .bodyValue(body) // Corrected from .bodyGift(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String resultText = ((Map)((java.util.List)response.get("content")).get(0)).get("text").toString();
            return resultText.trim().toLowerCase().contains("yes");
        } catch (Exception e) {
            System.err.println("Claude API error: " + e.getMessage());
            return false; // Fail-safe: allow comment if API fails
        }
    }
}