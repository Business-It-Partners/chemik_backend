package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}