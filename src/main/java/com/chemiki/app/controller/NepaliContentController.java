package com.chemiki.app.controller;

import com.chemiki.app.model.DetectionResult;
import com.chemiki.app.service.NepaliOffensiveDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/nepali")
@CrossOrigin(origins = "*") // Configure as needed
public class NepaliContentController {

    private final NepaliOffensiveDetectionService detectionService;

    @Autowired
    public NepaliContentController(NepaliOffensiveDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/detect")
    public ResponseEntity<DetectionResult> detectOffensive(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(DetectionResult.error("Text field is required and cannot be empty"));
        }

        DetectionResult result = detectionService.detectOffensiveContent(text);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/detect-async")
    public CompletableFuture<ResponseEntity<DetectionResult>> detectOffensiveAsync(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.trim().isEmpty()) {
            CompletableFuture<ResponseEntity<DetectionResult>> future = new CompletableFuture<>();
            future.complete(ResponseEntity.badRequest()
                    .body(DetectionResult.error("Text field is required and cannot be empty")));
            return future;
        }

        return detectionService.detectOffensiveContentAsync(text)
                .thenApply(result -> {
                    if (!result.isSuccess()) {
                        return ResponseEntity.badRequest().body(result);
                    }
                    return ResponseEntity.ok(result);
                });
    }

    @PostMapping("/batch-detect")
    public ResponseEntity<List<DetectionResult>> detectBatch(@RequestBody Map<String, List<String>> request) {
        List<String> texts = request.get("texts");

        if (texts == null || texts.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<DetectionResult> results = detectionService.detectBatchContent(texts);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateContent(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", "Text field is required and cannot be empty"
            ));
        }

        DetectionResult result = detectionService.detectOffensiveContent(text);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", result.getError()
            ));
        }

        boolean isValid = !result.getIsOffensive();

        Map<String, Object> response = Map.of(
                "valid", isValid,
                "text", text,
                "confidence", result.getConfidence(),
                "severity", result.getSeverity(),
                "message", isValid ? "Content approved" : "Content blocked - offensive language detected"
        );

        return ResponseEntity.ok(response);
    }

    // Simple GET endpoint for health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "Nepali Offensive Detection",
                "model", "abishektiwari/Nepali-Bert-offensiveDetection"
        ));
    }
}
