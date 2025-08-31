package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.RegisterDeviceTokenRequestDTO;
import com.chemiki.app.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    /**
     * Register device token for push notifications
     * POST /api/device-tokens/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<String> response = deviceTokenService.registerDeviceToken(userId, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "INVALID_DEVICE_TYPE":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }



}