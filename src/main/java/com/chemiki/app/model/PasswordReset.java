
// MODEL: PasswordReset - For password reset functionality
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Data
@Entity
@Table(name = "password_reset")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = true)
    private String phoneNumber; // For general users

    @Column(nullable = true)
    private String email; // For institutional users

    @Column(nullable = false)
    private String deliveryMethod; // "phone" or "email"

    @Column(nullable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        if (expiresAt == null) {
            expiresAt = Instant.now().plusSeconds(15 * 60); // 15 minutes
        }
    }
}