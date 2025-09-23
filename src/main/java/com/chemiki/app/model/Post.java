
// MODEL: Post - For social media posts
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "chemiki-posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // User who created the post

    @Column(columnDefinition = "TEXT")
    private String content; // Post text content

    @Column
    @ElementCollection
    private List<String> imagesUrls; // List of image URLs (optional)

    @Column(nullable = false)
    private Long viewCount = 0L; // Number of times post was viewed

    @Column(nullable = false)
    private Long commentCount = 0L; // Number of comments (for quick access)

    @Column(nullable = false)
    private String postType = "GENERAL"; // GENERAL, NEWS, NOTICE, ALERT, LOST_AND_FOUND

    @Column
    private String link; // Optional: Link

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Column
    private boolean deleted = false; // Soft delete flag

    @Column
    private boolean isActive = true; // Post visibility status
}
