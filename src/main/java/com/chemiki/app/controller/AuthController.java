package com.chemiki.app.controller;

import com.chemiki.app.config.JwtTokenProvider;
import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.LoginRequestDTO;
import com.chemiki.app.dto.requestDto.OtpVerificationRequestDTO;
import com.chemiki.app.dto.requestDto.RefreshTokenRequestDTO;
import com.chemiki.app.dto.requestDto.RegisterRequestDTO;
import com.chemiki.app.dto.responseDto.LoginResponseDTO;
import com.chemiki.app.dto.responseDto.OtpVerificationResponseDTO;
import com.chemiki.app.dto.responseDto.RegisterResponseDTO;
import com.chemiki.app.dto.responseDto.UserDetailResponseDTO;
import com.chemiki.app.service.LoginService;
import com.chemiki.app.service.OtpVerificationService;
import com.chemiki.app.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;
    private final OtpVerificationService otpVerificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {

       System.out.println(" this is the user type " +  request.isInstitutionalUser());
        ApiResponse<RegisterResponseDTO> response = registrationService.registerUser(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
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
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "AUTHENTICATION_FAILED":
                    status = HttpStatus.UNAUTHORIZED;
                    break;
                case "INVALID_INPUT":
                case "INVALID_LOGIN_METHOD":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token and generate new access token
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
            }

            // Generate new access token from refresh token
            String newAccessToken = jwtTokenProvider.generateAccessTokenFromRefresh(refreshToken);

            // Get username to generate new refresh token
            String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

            LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(responseDTO, "Token refreshed successfully"));

        } catch (Exception e) {
            String errorCode = "REFRESH_TOKEN_ERROR";
            String message = "Invalid or expired refresh token";

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(message, errorCode));
        }
    }

    @GetMapping("/user-details")
    public ResponseEntity<ApiResponse<UserDetailResponseDTO>> getUserDetails(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String phoneNumber) {
        ApiResponse<UserDetailResponseDTO> response = loginService.getUserDetails(userId, phoneNumber);
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