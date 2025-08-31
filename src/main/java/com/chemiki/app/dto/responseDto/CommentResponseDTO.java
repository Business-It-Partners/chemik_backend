package com.chemiki.app.dto.responseDto;

import lombok.Data;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for UI
    private String timeAgo; // "2 minutes ago", "1 hour ago"
    private boolean canDelete; // True if current user can delete this comment
}