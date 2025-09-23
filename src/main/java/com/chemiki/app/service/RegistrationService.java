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

            // Check if phone number already exists in User table
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                return ApiResponse.error("Phone number " + phoneNumber + " is already registered", "PHONE_NUMBER_EXISTS");
            }


             // Check if email already exists in User table (fully registered users)
            if (userRepository.findByEmail(email).isPresent()) {
                return ApiResponse.error("Email " + email + " is already registered", "EMAIL_EXISTS");
            }



             Optional<TempUser> existingTempUser = tempUserRepository.findByEmail(email);
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

                System.out.println("📝 Updating existing temp user registration for: " + email);

                // Clean up any existing OTPs for this phone number
                cleanupExistingOtps( email);
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

                System.out.println("🆕 Creating new temp user registration for: " + email);
            }

            tempUser = tempUserRepository.save(tempUser);

            // Generate OTP and create OTP record
            String otpCode = otpService.generateOtp();
            Otp otp = new Otp();
            otp.setToken(UUID.randomUUID().toString());
            otp.setOtp(otpCode);



            //For Sending Otp to user  users: use email for OTP
            otp.setEmail(email);
            otp.setDeliveryMethod("email");
            boolean otpSent   = otpService.sendOtpToEmail(email, otpCode, username);


            if (!otpSent) {
                return ApiResponse.error("Failed to send OTP. Please try again.", "OTP_DELIVERY_FAILED");
            }

            otpRepository.save(otp);

            // Build response
            RegisterResponseDTO response = new RegisterResponseDTO();
            response.setToken(otp.getToken());
            response.setEmail(email);

            String actionMessage = existingTempUser.isPresent() ? "New OTP sent to" : "OTP sent to";
            response.setMessage(actionMessage + " " + "Email " + ": " + email);

            return ApiResponse.success(response, response.getMessage());

        } catch (DataIntegrityViolationException e) {
            return ApiResponse.error("Phone number " + request.getPhoneNumber() + " is already in use", "DUPLICATE_PHONE_NUMBER");
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ApiResponse.error("An error occurred during registration", "INTERNAL_SERVER_ERROR");
        }
    }


    /**
     * Clean up any existing OTPs for the email
     */
    private void cleanupExistingOtps(String email) {
        try {
            otpRepository.findAll().stream()
                    .filter(otp -> email.equals(otp.getEmail()))
                    .forEach(otpRepository::delete);
            System.out.println("🧹 Cleaned up existing email OTPs for: " + email);
        } catch (Exception e) {
            System.err.println("Warning: Failed to cleanup existing OTPs: " + e.getMessage());
        }
    }

}