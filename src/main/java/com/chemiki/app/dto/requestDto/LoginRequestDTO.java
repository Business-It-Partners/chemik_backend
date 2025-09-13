// folder: com.chemiki.app.dto.requestDto/LoginRequestDTO.java
// purpose: DTO for user login request data supporting both phone and email
package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String phoneNumber; // For general users
    private String email;       // For institutional users
    private String password;

    // Note: User can send either phoneNumber OR email in the request body
    // The service will determine which one is provided and authenticate accordingly
}