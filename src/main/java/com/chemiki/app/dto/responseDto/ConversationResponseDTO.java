package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationResponseDTO {
    private Long otherUserId;
    private String otherUsername;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean hasUnreadMessages;
    private Long unreadCount;
}