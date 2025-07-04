// folder: com.chemiki.app.dto.requestDto/LoginRequestDTO.java
// purpose: DTO for user login request data (phone/email and password)
package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String phoneNumber; // can be phone number or email
    private String password;
}