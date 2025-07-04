package com.chemiki.app.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePhoto;
    private String coverPhoto;
    private boolean isInstitutionalUser;
    private boolean verified;

}