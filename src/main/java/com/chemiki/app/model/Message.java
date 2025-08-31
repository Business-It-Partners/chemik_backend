package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime readAt; // When the message was read

    @Column
    private boolean deleted = false; // Soft delete flag
}