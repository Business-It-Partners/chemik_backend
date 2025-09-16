package com.chemiki.app.service;

import com.chemiki.app.config.JwtTokenProvider;
import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.LoginRequestDTO;
import com.chemiki.app.dto.responseDto.LoginResponseDTO;
import com.chemiki.app.dto.responseDto.UserDetailResponseDTO;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO request) {
        try {
            String phoneNumber = request.getPhoneNumber();
            String email = request.getEmail();
            String password = request.getPassword();

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

            if (hasPhone) {
                user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);

                if (user != null && user.isInstitutionalUser()) {
                    return ApiResponse.error("Institutional users should login with email address", "INVALID_LOGIN_METHOD");
                }
            } else {
                // Find user by email
                Optional<User> userOptional = userRepository.findAll().stream()
                        .filter(u -> email.equals(u.getEmail()))
                        .findFirst();
                user = userOptional.orElse(null);

       //------------- enable if there is a need to restrict email logins to institutional users only -----------------
//                if (user != null && !user.isInstitutionalUser()) {
//                    return ApiResponse.error("Login with phone number", "INVALID_LOGIN_METHOD");
//                }
            }

            if (user == null) {
                String errorMessage = hasPhone ?
                        "No account found with this phone number" :
                        "No account found with this email address";
                return ApiResponse.error(errorMessage, "USER_NOT_FOUND");
            }

            // Authenticate using phone number (Spring Security expects phone as username)
            // Even for email logins, we authenticate using the phone number from the user record
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), password)
                );

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getPhoneNumber());

                LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();


                return ApiResponse.success(responseDTO, "Login successful");

            } catch (AuthenticationException e) {
                return ApiResponse.error("Invalid password", "AUTHENTICATION_FAILED");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("An error occurred during login", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<UserDetailResponseDTO> getUserDetails(Long userId, String phoneNumber) {
        try {
            // Validate input: ensure at least one parameter is provided
            if ((userId == null || userId <= 0) && (phoneNumber == null || phoneNumber.isEmpty())) {
                return ApiResponse.error("Either user ID or phone number is required", "INVALID_INPUT");
            }

            // Fetch user based on provided parameter
            User user;
            if (userId != null && userId > 0) {
                user = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "No account associated with this user ID"));
            } else {



                    user = userRepository.findByEmail(phoneNumber)
                            .orElseThrow(() ->
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "No account associated with this email address"));

            }
// comment is this
            // Map User to UserDetailResponseDTO
            UserDetailResponseDTO responseDTO = new UserDetailResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getProfilePhotoUrl(),
                    user.getCoverPhotoUrl(),
                    user.isInstitutionalUser(),
                    user.getDateOfBirth(),
                    user.getDistrict(),
                    user.getPalika(),
                    user.getWard(),
                    user.getInstitutionCategory(),
                    user.isVerified(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getWebsite()
            );

            return ApiResponse.success(responseDTO, "User details retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ApiResponse.error("An error occurred while retrieving user details", "INTERNAL_SERVER_ERROR");
        }
    }
}