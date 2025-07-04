package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.LoginRequestDTO;
import com.chemiki.app.dto.requestDto.OtpVerificationRequestDTO;
import com.chemiki.app.dto.requestDto.RegisterRequestDTO;
import com.chemiki.app.dto.responseDto.LoginResponseDTO;
import com.chemiki.app.dto.responseDto.OtpVerificationResponseDTO;
import com.chemiki.app.dto.responseDto.RegisterResponseDTO;
import com.chemiki.app.dto.responseDto.UserDetailResponseDTO;
import com.chemiki.app.service.LoginService;
import com.chemiki.app.service.OtpVerificationService;
import com.chemiki.app.service.RegistrationService;
import com.chemiki.app.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;
    private final OtpVerificationService otpVerificationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        ApiResponse<RegisterResponseDTO> response = registrationService.registerUser(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Map error codes to HTTP status codes
            String errorCode = response.getErrorCode(); // Use errorCode field
            HttpStatus status;
            switch (errorCode) {
                case "PHONE_NUMBER_EXISTS":
                case "DUPLICATE_PHONE_NUMBER":
                    status = HttpStatus.CONFLICT;
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

    @PostMapping("/otp-verification")
    public ResponseEntity<ApiResponse<OtpVerificationResponseDTO>> verifyOtp(@Valid @RequestBody OtpVerificationRequestDTO request) {
        ApiResponse<OtpVerificationResponseDTO> response = otpVerificationService.verifyOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Map error codes to HTTP status codes
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "PHONE_NUMBER_EXISTS":
                    status = HttpStatus.CONFLICT;
                    break;
                case "INVALID_INPUT":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case "INVALID_OTP":
                case "TEMP_USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        ApiResponse<LoginResponseDTO> response = loginService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Map error codes to HTTP status codes
            String errorCode = response.getErrorCode(); // Use errorCode field
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "AUTHENTICATION_FAILED":
                    status = HttpStatus.UNAUTHORIZED;
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



    //----------------  End point to get the user details information
    @GetMapping("/user-details")
    public ResponseEntity<ApiResponse<UserDetailResponseDTO>> getUserDetails(@RequestParam String phoneNumber) {
        ApiResponse<UserDetailResponseDTO> response = loginService.getUserDetails(phoneNumber);
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
}