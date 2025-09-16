package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}