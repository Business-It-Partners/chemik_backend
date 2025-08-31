package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.ForgotPasswordRequestDTO;
import com.chemiki.app.dto.requestDto.ResetPasswordRequestDTO;
import com.chemiki.app.dto.responseDto.ForgotPasswordResponseDTO;
import com.chemiki.app.dto.responseDto.ResetPasswordResponseDTO;
import com.chemiki.app.model.PasswordReset;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.PasswordResetRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    // Step 1: Request Password Reset
    public ApiResponse<ForgotPasswordResponseDTO> requestPasswordReset(ForgotPasswordRequestDTO request) {
        try {
            String phoneNumber = request.getPhoneNumber();

            // Input validation
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ApiResponse.error("Phone number is required", "INVALID_INPUT");
            }

            // Check if user exists with this phone number
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null) {
                return ApiResponse.error("No account found with this phone number", "USER_NOT_FOUND");
            }

            // Delete any existing reset request for this phone number
            passwordResetRepository.findByPhoneNumber(phoneNumber)
                    .ifPresent(passwordResetRepository::delete);

            // Create new password reset request
            PasswordReset passwordReset = new PasswordReset();
            passwordReset.setToken(UUID.randomUUID().toString());
            passwordReset.setOtp("1111"); // Mocked OTP (same as registration)
            passwordReset.setPhoneNumber(phoneNumber);
            passwordReset.setCreatedAt(LocalDateTime.now());
            passwordReset.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // 15 minutes expiry

            passwordReset = passwordResetRepository.save(passwordReset);

            // Build response
            ForgotPasswordResponseDTO response = new ForgotPasswordResponseDTO();
            response.setToken(passwordReset.getToken());
            response.setPhoneNumber(phoneNumber);
            response.setMessage("OTP sent to " + phoneNumber);

            return ApiResponse.success(response, response.getMessage());

        } catch (Exception e) {
            return ApiResponse.error("An error occurred during password reset request", "INTERNAL_SERVER_ERROR");
        }
    }

    // Step 2: Reset Password with OTP verification
    public ApiResponse<ResetPasswordResponseDTO> resetPassword(ResetPasswordRequestDTO request) {
        try {
            // Input validation
            String token = request.getToken();
            String phoneNumber = request.getPhoneNumber();
            String otpCode = request.getOtp();
            String newPassword = request.getNewPassword();

            if (token == null || token.isEmpty() ||
                    phoneNumber == null || phoneNumber.isEmpty() ||
                    otpCode == null || otpCode.isEmpty() ||
                    newPassword == null || newPassword.isEmpty()) {
                return ApiResponse.error("All fields are required", "INVALID_INPUT");
            }

            // Find password reset request
            PasswordReset passwordReset = passwordResetRepository
                    .findByTokenAndPhoneNumberAndExpiresAtAfter(token, phoneNumber, LocalDateTime.now())
                    .orElse(null);

            if (passwordReset == null) {
                return ApiResponse.error("Invalid or expired reset token", "INVALID_TOKEN");
            }

            // Validate OTP
            if (!passwordReset.getOtp().equals(otpCode)) {
                return ApiResponse.error("Invalid OTP code", "INVALID_OTP");
            }

            // Find user and update password
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found", "USER_NOT_FOUND");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Clean up - delete the password reset request
            passwordResetRepository.delete(passwordReset);

            // Build response
            ResetPasswordResponseDTO response = new ResetPasswordResponseDTO();
            response.setMessage("Password reset successfully");

            return ApiResponse.success(response, "Password reset successfully");

        } catch (Exception e) {
            return ApiResponse.error("An error occurred during password reset", "INTERNAL_SERVER_ERROR");
        }
    }
}