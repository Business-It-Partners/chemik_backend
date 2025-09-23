package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userProfilePicture; // User's profile picture URL
    private boolean isInstitutionalUser; // To show verified badge
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    // Additional fields for UI
    private String timeAgo; // "2 minutes ago", "1 hour ago"
    private boolean canDelete; // True if current user can delete this comment
}