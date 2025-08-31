package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userProfilePicture; // User's profile picture URL
    private boolean isInstitutionalUser; // To show verified badge
    private String content;
    private List<String> images;
    private String postType; // GENERAL, NEWS, NOTICE, ALERT, LOST_AND_FOUND
    private Long viewCount;
    private Long commentCount;
    private String link;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    // Additional fields for UI
    private String timeAgo; // "2 minutes ago", "1 hour ago"
    private boolean hasViewedByCurrentUser; // For current user tracking
}