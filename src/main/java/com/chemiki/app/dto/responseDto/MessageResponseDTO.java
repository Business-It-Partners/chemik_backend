package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.Instant;

@Data
public class MessageResponseDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderProfilePicture; // NEW: Sender's profile picture URL
    private Long receiverId;
    private String receiverUsername;
    private String receiverProfilePicture; // NEW: Receiver's profile picture URL
    private String content;
    private boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant readAt;
}