package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.ForgotPasswordRequestDTO;
import com.chemiki.app.dto.requestDto.ResetPasswordRequestDTO;
import com.chemiki.app.dto.responseDto.ForgotPasswordResponseDTO;
import com.chemiki.app.dto.responseDto.ResetPasswordResponseDTO;
import com.chemiki.app.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    // Step 1: Request password reset (send OTP)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponseDTO>> requestPasswordReset(
            @RequestBody ForgotPasswordRequestDTO request) {

        ApiResponse<ForgotPasswordResponseDTO> response = forgotPasswordService.requestPasswordReset(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "INVALID_INPUT":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }

    // Step 2: Reset password with OTP verification
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponseDTO>> resetPassword(
            @RequestBody ResetPasswordRequestDTO request) {

        ApiResponse<ResetPasswordResponseDTO> response = forgotPasswordService.resetPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "INVALID_TOKEN":
                case "INVALID_OTP":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "INVALID_INPUT":
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