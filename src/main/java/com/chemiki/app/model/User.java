
// MODEL: User - Main user entity
package com.chemiki.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column
    private String website;

    @Column(nullable = false)
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
    private boolean verified = false;

    @Column
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}