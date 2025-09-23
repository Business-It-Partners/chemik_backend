
// MODEL: DeviceToken - For FCM push notifications
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

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
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastUsedAt; // Track when token was last used successfully
}
