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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    // Step 1: Request Password Reset
    public ApiResponse<ForgotPasswordResponseDTO> requestPasswordReset(ForgotPasswordRequestDTO request) {
        try {
            String phoneNumber = request.getPhoneNumber();
            String email = request.getEmail();

            // Determine what identifier was provided
            boolean hasPhone = phoneNumber != null && !phoneNumber.trim().isEmpty();
            boolean hasEmail = email != null && !email.trim().isEmpty();

            if (!hasPhone && !hasEmail) {
                return ApiResponse.error("Either phone number or email is required", "INVALID_INPUT");
            }

            if (hasPhone && hasEmail) {
                return ApiResponse.error("Please provide either phone number or email, not both", "INVALID_INPUT");
            }

            // Find user based on provided identifier
            User user = null;
            String identifier = null;
            String deliveryMethod = null;

            if (hasPhone) {
                user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
                identifier = phoneNumber;
                deliveryMethod = "phone";
            } else {
                // Find user by email
                Optional<User> userOptional = userRepository.findAll().stream()
                        .filter(u -> email.equals(u.getEmail()))
                        .findFirst();
                user = userOptional.orElse(null);
                identifier = email;
                deliveryMethod = "email";
            }

            if (user == null) {
                String errorMessage = hasPhone ?
                        "No account found with this phone number" :
                        "No account found with this email address";
                return ApiResponse.error(errorMessage, "USER_NOT_FOUND");
            }




            // Clean up any existing reset request for this user
            cleanupExistingPasswordReset(user.isInstitutionalUser(), phoneNumber, email);

            // Generate OTP
            String otpCode = otpService.generateOtp();

            // Create new password reset request
            PasswordReset passwordReset = new PasswordReset();
            passwordReset.setToken(UUID.randomUUID().toString());
            passwordReset.setOtp(otpCode);
            passwordReset.setCreatedAt(LocalDateTime.now());
            passwordReset.setExpiresAt(LocalDateTime.now().plusMinutes(15));


            passwordReset.setEmail(email);
            passwordReset.setPhoneNumber(null);
            passwordReset.setDeliveryMethod("email");
            boolean otpSent  = otpService.sendPasswordResetOtpToEmail(email, otpCode, user.getUsername());





            if (!otpSent) {
                return ApiResponse.error("Failed to send password reset OTP. Please try again.", "OTP_DELIVERY_FAILED");
            }

            passwordReset = passwordResetRepository.save(passwordReset);

            // Build response
            ForgotPasswordResponseDTO response = new ForgotPasswordResponseDTO();
            response.setToken(passwordReset.getToken());
            response.setEmail(email);

            response.setMessage("Password reset OTP sent to " + "email" + ": " + email);


            return ApiResponse.success(response, response.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("An error occurred during password reset request", "INTERNAL_SERVER_ERROR");
        }
    }

    // Step 2: Reset Password with OTP verification
    public ApiResponse<ResetPasswordResponseDTO> resetPassword(ResetPasswordRequestDTO request) {
        try {
            // Input validation
            String token = request.getToken();
             String otpCode = request.getOtp();
            String newPassword = request.getNewPassword();

            if (token == null || token.isEmpty() || otpCode == null || otpCode.isEmpty() ||
                    newPassword == null || newPassword.isEmpty()) {
                return ApiResponse.error("Token, OTP, and new password are required", "INVALID_INPUT");
            }

            // Find password reset request by token first
            PasswordReset passwordReset = passwordResetRepository
                    .findByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                    .orElse(null);

            if (passwordReset == null) {
                return ApiResponse.error("Invalid or expired reset token", "INVALID_TOKEN");
            }

            // Validate OTP
            if (!passwordReset.getOtp().equals(otpCode)) {
                return ApiResponse.error("Invalid OTP code", "INVALID_OTP");
            }

            // Find user based on the delivery method used
            User user = null;
            if ("phone".equals(passwordReset.getDeliveryMethod())) {
                user = userRepository.findByPhoneNumber(passwordReset.getPhoneNumber()).orElse(null);
            } else if ("email".equals(passwordReset.getDeliveryMethod())) {
                Optional<User> userOptional = userRepository.findAll().stream()
                        .filter(u -> passwordReset.getEmail().equals(u.getEmail()))
                        .findFirst();
                user = userOptional.orElse(null);
            }

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
            e.printStackTrace();
            return ApiResponse.error("An error occurred during password reset", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * Clean up any existing password reset requests for the user
     */
    private void cleanupExistingPasswordReset(boolean isInstitutional, String phoneNumber, String email) {
        try {


            passwordResetRepository.findByEmail(email).ifPresent(passwordResetRepository::delete);
            System.out.println("🧹 Cleaned up existing email password reset for: " + email);


            // ------ this is for when we do have the proper seperation of institutional and general users -------
//            if (isInstitutional && email != null) {
//                // For institutional users, clean up email-based resets
//                passwordResetRepository.findByEmail(email).ifPresent(passwordResetRepository::delete);
//                System.out.println("🧹 Cleaned up existing email password reset for: " + email);
//            } else if (!isInstitutional && phoneNumber != null) {
//                // For general users, clean up phone-based resets
//                passwordResetRepository.findByPhoneNumber(phoneNumber).ifPresent(passwordResetRepository::delete);
//                System.out.println("🧹 Cleaned up existing phone password reset for: " + phoneNumber);
//            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to cleanup existing password reset: " + e.getMessage());
            // Don't fail the reset process if cleanup fails
        }
    }
}