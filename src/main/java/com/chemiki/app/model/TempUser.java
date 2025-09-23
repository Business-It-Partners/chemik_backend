
// MODEL: TempUser - For temporary user registration
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Data
@Entity
@Table(name = "temporary_users")
public class TempUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column
    private String profilePhotoUrl;

    @Column
    private String coverPhotoUrl;

    @Column(nullable = false)
    private boolean institutionalUser;

    @Column
    private String dateOfBirth;

    @Column
    private String district;

    @Column
    private String palika;

    @Column
    private String ward;

    @Column
    private String institutionCategory;

    @Column(nullable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        if (expiresAt == null) {
            expiresAt = Instant.now().plusSeconds(30 * 60); // 30 minutes
        }
    }
}
