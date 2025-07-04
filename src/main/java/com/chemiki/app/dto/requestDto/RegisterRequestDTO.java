package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String phoneNumber;
    private String email;
    private boolean isInstitutionalUser;
}