package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String profilePhotoUrl;

    @Column
    private String coverPhotoUrl;

    @Column(nullable = false)
    private boolean isInstitutionalUser;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();
}