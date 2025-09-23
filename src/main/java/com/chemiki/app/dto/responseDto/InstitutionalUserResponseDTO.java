package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

  }