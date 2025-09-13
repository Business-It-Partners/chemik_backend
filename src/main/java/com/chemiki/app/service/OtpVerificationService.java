package com.chemiki.app.service;

import com.chemiki.app.dto.requestDto.OtpVerificationRequestDTO;
import com.chemiki.app.dto.responseDto.OtpVerificationResponseDTO;
import com.chemiki.app.model.Otp;
import com.chemiki.app.model.TempUser;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.OtpRepository;
import com.chemiki.app.repository.TempUserRepository;
import com.chemiki.app.repository.UserRepository;
import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpVerificationService {

    private final OtpRepository otpRepository;
    private final TempUserRepository tempUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<OtpVerificationResponseDTO> verifyOtp(OtpVerificationRequestDTO request) {
        try {
            // Input validation
            String token = request.getToken();
            String phoneNumber = request.getPhoneNumber();
            String otpCode = request.getOtp();

            if (token == null || token.isEmpty() || otpCode == null || otpCode.isEmpty()) {
                return ApiResponse.error("Token and OTP code are required", "INVALID_INPUT");
            }

            // Find TempUser first to determine user type
            TempUser tempUser = tempUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Temp user not found"));

            boolean isInstitutional = tempUser.isInstitutionalUser();

            // Find OTP based on user type
            Otp otp;
            if (isInstitutional) {
                // For institutional users: verify using email
                String email = tempUser.getEmail();
                if (email == null || email.isEmpty()) {
                    return ApiResponse.error("Email not found for institutional user", "EMAIL_NOT_FOUND");
                }
                otp = otpRepository.findByTokenAndEmailAndExpiresAtAfter(token, email, LocalDateTime.now())
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired OTP"));
            } else {
                // For general users: verify using phone number
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    return ApiResponse.error("Phone number is required", "PHONE_NUMBER_REQUIRED");
                }
                otp = otpRepository.findByTokenAndPhoneNumberAndExpiresAtAfter(token, phoneNumber, LocalDateTime.now())
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired OTP"));
            }

            // Validate OTP code
            if (!otp.getOtp().equals(otpCode)) {
                return ApiResponse.error("Invalid OTP code", "INVALID_OTP");
            }

            // Check if phone number already exists in User table
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                return ApiResponse.error("Phone number " + phoneNumber + " is already registered", "PHONE_NUMBER_EXISTS");
            }

            // Create and save User
            User user = new User();
            user.setUsername(tempUser.getUsername());
            user.setPassword(passwordEncoder.encode(tempUser.getPassword()));
            user.setEmail(tempUser.getEmail());
            user.setPhoneNumber(tempUser.getPhoneNumber());
            user.setInstitutionalUser(tempUser.isInstitutionalUser());
            user.setProfilePhotoUrl(tempUser.getProfilePhotoUrl());
            user.setCoverPhotoUrl(tempUser.getCoverPhotoUrl());
            user.setDateOfBirth(tempUser.getDateOfBirth());
            user.setDistrict(tempUser.getDistrict());
            user.setPalika(tempUser.getPalika());
            user.setWard(tempUser.getWard());
            user.setInstitutionCategory(tempUser.getInstitutionCategory());
            user.setVerified(false);
            user.setWebsite("");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Clean up
            otpRepository.delete(otp);
            tempUserRepository.delete(tempUser);

            OtpVerificationResponseDTO response = new OtpVerificationResponseDTO();
            String userType = isInstitutional ? "Institutional" : "Individual";
            response.setMessage(userType + " account registered successfully");

            return ApiResponse.success(response, response.getMessage());
        } catch (ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage(), e.getMessage().contains("OTP") ? "INVALID_OTP" : "TEMP_USER_NOT_FOUND");
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ApiResponse.error("An error occurred during OTP verification", "INTERNAL_SERVER_ERROR");
        }
    }
}