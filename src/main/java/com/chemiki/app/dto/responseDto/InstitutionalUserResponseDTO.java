package com.chemiki.app.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionalUserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
    private String institutionCategory;
    private String district;
    private String palika;
    private String ward;
    private boolean isVerified;
    private LocalDateTime createdAt;

  }