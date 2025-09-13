package com.chemiki.app.service;

import com.chemiki.app.config.CustomUserDetails;
import com.chemiki.app.dto.responseDto.RegisterResponseDTO;
import com.chemiki.app.dto.requestDto.RegisterRequestDTO;
import com.chemiki.app.model.Otp;
import com.chemiki.app.model.TempUser;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.OtpRepository;
import com.chemiki.app.repository.TempUserRepository;
import com.chemiki.app.repository.UserRepository;
import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TempUserRepository tempUserRepository;
    private final OtpRepository otpRepository;
    private final OtpService otpService;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + phoneNumber));
        return new CustomUserDetails(user);
    }

    public ApiResponse<RegisterResponseDTO> registerUser(RegisterRequestDTO request) {
        try {
            // Input validation
            String phoneNumber = request.getPhoneNumber();
            String username = request.getUsername();
            String password = request.getPassword();
            String email = request.getEmail();
            boolean isInstitutional = request.isInstitutionalUser();

            // Check if phone number already exists in User table (fully registered users)
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                return ApiResponse.error("Phone number " + phoneNumber + " is already registered", "PHONE_NUMBER_EXISTS");
            }

            // Validation for institutional users
            if (isInstitutional && (email == null || email.trim().isEmpty())) {
                return ApiResponse.error("Email is required for institutional users", "EMAIL_REQUIRED");
            }

            // Check if phone number exists in TempUser table (incomplete registration)
            Optional<TempUser> existingTempUser = tempUserRepository.findByPhoneNumber(phoneNumber);
            TempUser tempUser;

            if (existingTempUser.isPresent()) {
                // Update existing temp user with new registration data
                tempUser = existingTempUser.get();
                tempUser.setUsername(username);
                tempUser.setPassword(password);
                tempUser.setEmail(email);
                tempUser.setInstitutionalUser(isInstitutional);
                tempUser.setProfilePhotoUrl(request.getProfilePhotoUrl());
                tempUser.setCoverPhotoUrl(request.getCoverPhotoUrl());
                tempUser.setDateOfBirth(request.getDateOfBirth());
                tempUser.setDistrict(request.getDistrict());
                tempUser.setPalika(request.getPalika());
                tempUser.setWard(request.getWard());
                tempUser.setInstitutionCategory(request.getInstitutionCategory());
                tempUser.setCreatedAt(LocalDateTime.now()); // Reset creation time
                tempUser.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // Reset expiry

                System.out.println("📝 Updating existing temp user registration for: " + phoneNumber);

                // Clean up any existing OTPs for this phone number
                cleanupExistingOtps(phoneNumber, isInstitutional, email);
            } else {
                // Create new temp user
                tempUser = new TempUser();
                tempUser.setUsername(username);
                tempUser.setPassword(password);
                tempUser.setEmail(email);
                tempUser.setPhoneNumber(phoneNumber);
                tempUser.setInstitutionalUser(isInstitutional);
                tempUser.setProfilePhotoUrl(request.getProfilePhotoUrl());
                tempUser.setCoverPhotoUrl(request.getCoverPhotoUrl());
                tempUser.setDateOfBirth(request.getDateOfBirth());
                tempUser.setDistrict(request.getDistrict());
                tempUser.setPalika(request.getPalika());
                tempUser.setWard(request.getWard());
                tempUser.setInstitutionCategory(request.getInstitutionCategory());
                tempUser.setCreatedAt(LocalDateTime.now());
                tempUser.setExpiresAt(LocalDateTime.now().plusMinutes(30));

                System.out.println("🆕 Creating new temp user registration for: " + phoneNumber);
            }

            tempUser = tempUserRepository.save(tempUser);

            // Generate OTP and create OTP record
            String otpCode = otpService.generateOtp();
            Otp otp = new Otp();
            otp.setToken(UUID.randomUUID().toString());
            otp.setOtp(otpCode);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(15));

            boolean otpSent = false;
            if (isInstitutional) {
                // For institutional users: use email for OTP
                otp.setEmail(email);
                otp.setPhoneNumber(null);
                otp.setDeliveryMethod("email");
                otpSent = otpService.sendOtpToEmail(email, otpCode, username);
            } else {
                // For general users: use phone for OTP
                otp.setPhoneNumber(phoneNumber);
                otp.setEmail(null);
                otp.setDeliveryMethod("phone");
                otpSent = otpService.sendOtpToPhone(phoneNumber, otpCode, username);
            }

            if (!otpSent) {
                return ApiResponse.error("Failed to send OTP. Please try again.", "OTP_DELIVERY_FAILED");
            }

            otpRepository.save(otp);

            // Build response
            RegisterResponseDTO response = new RegisterResponseDTO();
            response.setToken(otp.getToken());
            response.setPhoneNumber(phoneNumber);

            String deliveryTarget = isInstitutional ? email : phoneNumber;
            String deliveryMethod = isInstitutional ? "email" : "phone number";
            String actionMessage = existingTempUser.isPresent() ? "New OTP sent to" : "OTP sent to";
            response.setMessage(actionMessage + " " + deliveryMethod + ": " + deliveryTarget);

            return ApiResponse.success(response, response.getMessage());

        } catch (DataIntegrityViolationException e) {
            return ApiResponse.error("Phone number " + request.getPhoneNumber() + " is already in use", "DUPLICATE_PHONE_NUMBER");
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ApiResponse.error("An error occurred during registration", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * Clean up any existing OTPs for the phone number/email to prevent conflicts
     */
    private void cleanupExistingOtps(String phoneNumber, boolean isInstitutional, String email) {
        try {
            if (isInstitutional && email != null) {
                // For institutional users, clean up email-based OTPs
                otpRepository.findAll().stream()
                        .filter(otp -> email.equals(otp.getEmail()))
                        .forEach(otpRepository::delete);
                System.out.println("🧹 Cleaned up existing email OTPs for: " + email);
            } else {
                // For general users, clean up phone-based OTPs
                otpRepository.findAll().stream()
                        .filter(otp -> phoneNumber.equals(otp.getPhoneNumber()))
                        .forEach(otpRepository::delete);
                System.out.println("🧹 Cleaned up existing phone OTPs for: " + phoneNumber);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to cleanup existing OTPs: " + e.getMessage());
            // Don't fail the registration process if cleanup fails
        }
    }
}