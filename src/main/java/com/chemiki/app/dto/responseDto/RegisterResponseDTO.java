package com.chemiki.app.dto.responseDto;

import lombok.Data;

@Data
public class RegisterResponseDTO {
    private String token;
    private String email;
    private String message;
}



