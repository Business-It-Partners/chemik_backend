package com.chemiki.app.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserDetailResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
    private boolean isInstitutionalUser;
    // add website as
    private String dateOfBirth;
    private String district;
    private String palika;
    private String ward;
    private String institutionCategory;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String website;
}