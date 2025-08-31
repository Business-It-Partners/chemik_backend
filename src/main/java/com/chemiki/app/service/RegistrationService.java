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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TempUserRepository tempUserRepository;
    private final OtpRepository otpRepository;

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

            // Check if phone number already exists in User table
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                return ApiResponse.error("Phone number " + phoneNumber + " is already registered", "PHONE_NUMBER_EXISTS");
            }

            // Create and save TempUser
            TempUser tempUser = new TempUser();
            tempUser.setUsername(username);
            tempUser.setPassword(password);
            tempUser.setEmail(email);
            tempUser.setPhoneNumber(phoneNumber);
            tempUser.setInstitutionalUser(request.isInstitutionalUser());
            tempUser.setProfilePhotoUrl(request.getProfilePhotoUrl());
            tempUser.setCoverPhotoUrl(request.getCoverPhotoUrl());
            tempUser.setDateOfBirth(request.getDateOfBirth());
            tempUser.setDistrict(request.getDistrict());
            tempUser.setPalika(request.getPalika());
            tempUser.setWard(request.getWard());
            tempUser.setInstitutionCategory(request.getInstitutionCategory());
            tempUser.setCreatedAt(LocalDateTime.now());
            tempUser.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            tempUser = tempUserRepository.save(tempUser);

            // Create and save OTP
            Otp otp = new Otp();
            otp.setToken(UUID.randomUUID().toString());
            otp.setOtp("1111"); // Consider generating a random OTP in production
            otp.setPhoneNumber(phoneNumber);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            otpRepository.save(otp);

            // Build response
            RegisterResponseDTO response = new RegisterResponseDTO();
            response.setToken(otp.getToken());
            response.setPhoneNumber(phoneNumber);
            response.setMessage("OTP sent to " + phoneNumber);

            return ApiResponse.success(response, response.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponse.error("Phone number " + request.getPhoneNumber() + " is already in use", "DUPLICATE_PHONE_NUMBER");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred during registration", "INTERNAL_SERVER_ERROR");
        }
    }
}