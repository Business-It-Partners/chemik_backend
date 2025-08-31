package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId; // Which post this comment belongs to

    @Column(nullable = false)
    private Long userId; // User who wrote the comment

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Comment text

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private boolean deleted = false; // Soft delete flag

    // Note: No parentCommentId - we only want linear comments, no replies
}