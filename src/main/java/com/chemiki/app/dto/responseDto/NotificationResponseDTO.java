package com.chemiki.app.dto.responseDto;
public class NotificationResponseDTO {
    private String messageId;
    private boolean success;
    private String message;

    public NotificationResponseDTO(String messageId, boolean success, String message) {
        this.messageId = messageId;
        this.success = success;
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}