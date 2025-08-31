package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_views")
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId; // Which post was viewed

    @Column(nullable = false)
    private Long userId; // Who viewed the post

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    // Unique constraint to prevent duplicate views from same user
    @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "userId"}))
    public static class PostViewConstraint {}
}