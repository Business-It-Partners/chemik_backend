package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
}