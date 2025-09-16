package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String token;
    private String otp;
    private String email;
    private String newPassword;
}