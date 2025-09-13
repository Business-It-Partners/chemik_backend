package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.RegisterRequestDTO;
import com.chemiki.app.dto.responseDto.InstitutionalUserResponseDTO;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.PostRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionalUserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all institutional users for explore section
     */
    public ApiResponse<List<InstitutionalUserResponseDTO>> getAllInstitutionalUsers() {
        try {
//             List<User> institutionalUsers = userRepository.findAll();

            // LATER: Uncomment this line to fetch only institutional users
             List<User> institutionalUsers = userRepository.findByInstitutionalUserTrue();

            List<InstitutionalUserResponseDTO> responseDTOs = institutionalUsers.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "Institutional users retrieved successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving institutional users", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * Create a new institutional user directly, marking it as verified
     */
    public ApiResponse<InstitutionalUserResponseDTO> createInstitutionalUser(RegisterRequestDTO request) {
        try {
            // Input validation
            if (request.getPhoneNumber() == null || request.getUsername() == null || request.getPassword() == null) {
                return ApiResponse.error("Phone number, username, and password are required", "INVALID_INPUT");
            }

            // Check if phone number or username already exists
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                return ApiResponse.error("Phone number " + request.getPhoneNumber() + " is already registered", "PHONE_NUMBER_EXISTS");
            }


            // Create and save User
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
            user.setCoverPhotoUrl(request.getCoverPhotoUrl());
            user.setInstitutionalUser(true); // Mark as institutional user
            user.setDateOfBirth(request.getDateOfBirth());
            user.setDistrict(request.getDistrict());
            user.setPalika(request.getPalika());
            user.setWard(request.getWard());
            user.setInstitutionCategory(request.getInstitutionCategory());
            user.setVerified(true); // Mark as verified
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);

            // Convert to response DTO
            InstitutionalUserResponseDTO responseDTO = convertToResponseDTO(user);
            return ApiResponse.success(responseDTO, "Institutional user created successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while creating institutional user", "INTERNAL_SERVER_ERROR");
        }
    }


    /**
     * Convert User entity to InstitutionalUserResponseDTO
     */
    private InstitutionalUserResponseDTO convertToResponseDTO(User user) {
        InstitutionalUserResponseDTO dto = new InstitutionalUserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
        dto.setCoverPhotoUrl(user.getCoverPhotoUrl());
        dto.setInstitutionCategory(user.getInstitutionCategory());
        dto.setDistrict(user.getDistrict());
        dto.setPalika(user.getPalika());
        dto.setWard(user.getWard());
        dto.setVerified(user.isVerified());
        dto.setCreatedAt(user.getCreatedAt());



        return dto;
    }
}
