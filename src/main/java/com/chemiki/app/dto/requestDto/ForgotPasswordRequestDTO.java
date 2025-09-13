package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    private String phoneNumber; // For general users
    private String email;       // For institutional users

    // Note: User can send either phoneNumber OR email in the request body
    // The service will determine which one is provided and act accordingly
}