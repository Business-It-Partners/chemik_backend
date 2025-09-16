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
            String email = request.getEmail();
            String otpCode = request.getOtp();

            if (token == null || token.isEmpty() || otpCode == null || otpCode.isEmpty()) {
                return ApiResponse.error("Token and OTP code are required", "INVALID_INPUT");
            }

            // Find TempUser first to determine user type
            TempUser tempUser = tempUserRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Temp user not found"));


            // Find OTP based on user type
            Otp otp;
            otp = otpRepository.findByTokenAndEmailAndExpiresAtAfter(token, email, LocalDateTime.now())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired OTP"));

            // Validate OTP code
            if (!otp.getOtp().equals(otpCode)) {
                return ApiResponse.error("Invalid OTP code", "INVALID_OTP");
            }

            // Check if email already exists in User table
            if (userRepository.findByEmail(email).isPresent()) {
                return ApiResponse.error("Email " + email + " is already registered", "EMAIL_EXISTS");
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
             response.setMessage(" Account registered successfully");

            return ApiResponse.success(response, response.getMessage());
        } catch (ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage(), e.getMessage().contains("OTP") ? "INVALID_OTP" : "TEMP_USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred during OTP verification", "INTERNAL_SERVER_ERROR");
        }
    }
}