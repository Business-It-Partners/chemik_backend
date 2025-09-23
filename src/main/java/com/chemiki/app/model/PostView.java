
// MODEL: PostView - For tracking post views
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Data
@Entity
@Table(name = "post_views", uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "userId"}))
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId; // Which post was viewed

    @Column(nullable = false)
    private Long userId; // Who viewed the post

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant viewedAt;
}
