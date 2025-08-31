package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDeviceTokenRequestDTO {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    @NotBlank(message = "Device type is required")
    private String deviceType; // "ANDROID", "IOS", "WEB"

    private String deviceInfo; // Optional: device model, browser info, etc.
}