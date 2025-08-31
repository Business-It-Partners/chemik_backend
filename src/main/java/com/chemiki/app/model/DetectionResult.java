package com.chemiki.app.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetectionResult {
    private boolean success;
    private String inputText;
    private Boolean isOffensive;
    private Double confidence;
    private Double offensiveScore;
    private Double cleanScore;
    private String severity;
    private String flag;
    private String message;
    private String error;
    private LocalDateTime timestamp;

    private DetectionResult() {
        this.timestamp = LocalDateTime.now();
    }

    // Static factory methods
    public static DetectionResult success(String inputText, boolean isOffensive, double confidence,
                                          double offensiveScore, double cleanScore, String severity, String flag) {
        DetectionResult result = new DetectionResult();
        result.success = true;
        result.inputText = inputText;
        result.isOffensive = isOffensive;
        result.confidence = confidence;
        result.offensiveScore = offensiveScore;
        result.cleanScore = cleanScore;
        result.severity = severity;
        result.flag = flag;
        result.message = isOffensive ? "Offensive content detected" : "Content appears clean";
        return result;
    }

    public static DetectionResult error(String errorMessage) {
        DetectionResult result = new DetectionResult();
        result.success = false;
        result.error = errorMessage;
        return result;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public String getInputText() { return inputText; }
    public Boolean getIsOffensive() { return isOffensive; }
    public Double getConfidence() { return confidence; }
    public Double getOffensiveScore() { return offensiveScore; }
    public Double getCleanScore() { return cleanScore; }
    public String getSeverity() { return severity; }
    public String getFlag() { return flag; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        if (!success) {
            return String.format("DetectionResult{success=false, error='%s'}", error);
        }
        return String.format("DetectionResult{success=true, isOffensive=%s, confidence=%.4f, severity='%s'}",
                isOffensive, confidence, severity);
    }
}