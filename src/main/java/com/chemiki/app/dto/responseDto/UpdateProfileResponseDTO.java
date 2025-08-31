package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateProfileResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
    private boolean isInstitutionalUser;
    private String dateOfBirth;
    private String district;
    private String palika;
    private String ward;
    private String institutionCategory;
    private boolean isVerified;
    private String website; // Added website field
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}