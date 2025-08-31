package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Which user this token belongs to

    @Column(nullable = false, unique = true, length = 500)
    private String fcmToken; // Firebase Cloud Messaging token

    @Column(nullable = false)
    private String deviceType; // "ANDROID", "IOS", "WEB"

    @Column
    private String deviceInfo; // Optional: device model, browser info, etc.

    @Column(nullable = false)
    private boolean isActive = true; // To handle token expiry/deactivation

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastUsedAt = LocalDateTime.now(); // Track when token was last used successfully
}