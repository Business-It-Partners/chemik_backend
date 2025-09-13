package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String phoneNumber;
    private String email;

    private boolean institutionalUser;
    private String dateOfBirth;
    private String district;
    private String palika;
    private String ward;
    private String institutionCategory;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
}