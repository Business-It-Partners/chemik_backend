package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
 
import java.time.Instant;

@Data
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId; // User who sent the message

    @Column(nullable = false)
    private Long receiverId; // User who receives the message

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Message content

    @Column(nullable = false)
    private boolean isRead = false; // Message read status

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant readAt; // When the message was read

    @Column
    private boolean deleted = false; // Soft delete flag
}