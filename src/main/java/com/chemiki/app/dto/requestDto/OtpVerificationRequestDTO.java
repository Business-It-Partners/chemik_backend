// folder: com.chemiki.app.dto.requestDto
// purpose: DTO for OTP verification request data
package com.chemiki.app.dto.requestDto;

import lombok.Data;

@Data
public class OtpVerificationRequestDTO {
    private String token;
    private String otp;
    private String phoneNumber;
}