package com.chemiki.app.service;



import com.chemiki.app.config.HuggingFaceConfig;
import com.chemiki.app.model.DetectionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class NepaliOffensiveDetectionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HuggingFaceConfig config;
    private final String apiUrl;

    @Autowired
    public NepaliOffensiveDetectionService(RestTemplate restTemplate,
                                           HuggingFaceConfig config) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.config = config;
        this.apiUrl = "https://api-inference.huggingface.co/models/" + config.getModelName();
    }

    /**
     * Detect offensive content in Nepali text
     * @param nepaliText The Nepali text to analyze
     * @return DetectionResult with analysis results
     */
    public DetectionResult detectOffensiveContent(String nepaliText) {
        if (nepaliText == null || nepaliText.trim().isEmpty()) {
            return DetectionResult.error("Empty text provided");
        }

        try {
            // Prepare headers
            HttpHeaders headers = createHeaders();

            // Prepare request body for HuggingFace Inference API
            Map<String, Object> requestBody = createRequestBody(nepaliText);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseHuggingFaceResponse(response.getBody(), nepaliText);
            } else {
                return DetectionResult.error("API call failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            return DetectionResult.error("Network error: " + e.getMessage());
        } catch (Exception e) {
            return DetectionResult.error("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Async version for better performance
     */
    public CompletableFuture<DetectionResult> detectOffensiveContentAsync(String nepaliText) {
        return CompletableFuture.supplyAsync(() -> detectOffensiveContent(nepaliText));
    }

    /**
     * Batch processing for multiple texts
     */
    public List<DetectionResult> detectBatchContent(List<String> texts) {
        List<DetectionResult> results = new ArrayList<>();

        for (String text : texts) {
            if (text != null && !text.trim().isEmpty()) {
                results.add(detectOffensiveContent(text.trim()));

                // Small delay to respect rate limits
                try {
                    Thread.sleep(200); // 200ms delay between requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                results.add(DetectionResult.error("Empty text in batch"));
            }
        }

        return results;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        // Add authorization if token is provided
        if (config.getApiToken() != null && !config.getApiToken().isEmpty()) {
            headers.setBearerAuth(config.getApiToken());
        }

        return headers;
    }

    private Map<String, Object> createRequestBody(String text) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", text);

        // Options for better results
        Map<String, Object> options = new HashMap<>();
        options.put("wait_for_model", true);
        options.put("use_cache", false);
        requestBody.put("options", options);

        return requestBody;
    }

    private DetectionResult parseHuggingFaceResponse(String responseBody, String inputText) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);

            // Handle different response formats from HuggingFace
            if (response.isArray() && response.size() > 0) {
                JsonNode firstResult = response.get(0);

                if (firstResult.isArray()) {
                    // Classification format: [[{"label": "LABEL_0", "score": 0.8}, {"label": "LABEL_1", "score": 0.2}]]
                    return parseClassificationResponse(firstResult, inputText);
                } else if (firstResult.has("label") && firstResult.has("score")) {
                    // Single prediction format: [{"label": "LABEL_1", "score": 0.8}]
                    return parseSinglePrediction(response, inputText);
                }
            }

            return DetectionResult.error("Unexpected response format from HuggingFace API");

        } catch (Exception e) {
            return DetectionResult.error("Error parsing API response: " + e.getMessage());
        }
    }

    private DetectionResult parseClassificationResponse(JsonNode classifications, String inputText) {
        double offensiveScore = 0.0;
        double cleanScore = 0.0;

        for (JsonNode classification : classifications) {
            String label = classification.get("label").asText();
            double score = classification.get("score").asDouble();

            if ("LABEL_1".equals(label)) {
                offensiveScore = score;
            } else if ("LABEL_0".equals(label)) {
                cleanScore = score;
            }
        }

        boolean isOffensive = offensiveScore > cleanScore;
        double confidence = Math.max(offensiveScore, cleanScore);

        return DetectionResult.success(
                inputText,
                isOffensive,
                confidence,
                offensiveScore,
                cleanScore,
                determineSeverity(confidence, isOffensive),
                isOffensive ? "🚩" : "✅"
        );
    }

    private DetectionResult parseSinglePrediction(JsonNode response, String inputText) {
        JsonNode prediction = response.get(0);
        String label = prediction.get("label").asText();
        double score = prediction.get("score").asDouble();

        boolean isOffensive = "LABEL_1".equals(label);

        return DetectionResult.success(
                inputText,
                isOffensive,
                score,
                isOffensive ? score : 0.0,
                isOffensive ? 0.0 : score,
                determineSeverity(score, isOffensive),
                isOffensive ? "🚩" : "✅"
        );
    }

    private String determineSeverity(double confidence, boolean isOffensive) {
        if (!isOffensive) return "SAFE";

        if (confidence >= 0.9) return "VERY_HIGH";
        if (confidence >= 0.8) return "HIGH";
        if (confidence >= 0.6) return "MEDIUM";
        if (confidence >= 0.4) return "LOW";
        return "MINIMAL";
    }
}
