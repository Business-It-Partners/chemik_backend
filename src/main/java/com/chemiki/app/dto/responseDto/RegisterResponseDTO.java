package com.chemiki.app.dto.responseDto;

import lombok.Data;

@Data
public class RegisterResponseDTO {
    private String token;
    private String phoneNumber;
    private String message;
}