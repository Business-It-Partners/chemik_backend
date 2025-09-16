package com.chemiki.app.dto.responseDto;

import lombok.Data;

@Data
public class ForgotPasswordResponseDTO {
    private String token;
    private String email;
    private String message;
}