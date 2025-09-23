package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.Instant;

@Data
public class ConversationResponseDTO {
    private Long otherUserId;
    private String otherUsername;
    private String lastMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastMessageTime;

    private boolean hasUnreadMessages;
    private String profilePicture; // NEW: Profile picture URL
    private Long unreadCount;
}