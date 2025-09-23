package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
    private boolean isActive;

    // Additional fields for UI
    private String timeAgo; // "2 minutes ago", "1 hour ago"
    private boolean hasViewedByCurrentUser; // For current user tracking
}