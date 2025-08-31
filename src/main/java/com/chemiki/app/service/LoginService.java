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

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO request) {
        try {
            String phoneNumber = request.getPhoneNumber();
            String password = request.getPassword();

            // Check if user exists
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "No account associated with this phone number"));

            // Authenticate
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(phoneNumber, password)
                );

                // Generate tokens (both are JWTs, no database storage)
                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken(phoneNumber);

                LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                return ApiResponse.success(responseDTO, "Login successful");
            } catch (AuthenticationException e) {
                return ApiResponse.error(e.getMessage(), "AUTHENTICATION_FAILED");
            }
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
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
                user = userRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "No account associated with this phone number"));
            }

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
                    user.getUpdatedAt()
            );

            return ApiResponse.success(responseDTO, "User details retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving user details", "INTERNAL_SERVER_ERROR");
        }
    }
}