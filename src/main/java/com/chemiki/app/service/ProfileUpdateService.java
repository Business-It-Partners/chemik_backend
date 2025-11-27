package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.UpdateProfileRequestDTO;
import com.chemiki.app.dto.responseDto.UpdateProfileResponseDTO;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {

    private final UserRepository userRepository;

    // Dynamic base URL configuration (same as PostService)
    @Value("${app.base-url}")
    private String baseUrl;

    public ApiResponse<UpdateProfileResponseDTO> updateProfile(UpdateProfileRequestDTO request, Long userId) {
        try {
            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Check if username is being changed and is unique
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()
                    && !request.getUsername().equals(user.getUsername())) {

                user.setUsername(request.getUsername().trim());
            }

            // Update email (only if provided and not empty)
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail().trim());
            }

            // Update website (only if provided)
            if (request.getWebsite() != null) {
                // Allow empty string to clear website
                user.setWebsite(request.getWebsite().trim().isEmpty() ? null : request.getWebsite().trim());
            }

            // Handle profile picture upload
            if (request.hasProfilePicture()) {
                String profilePictureUrl = uploadImage(request.getProfilePicture(), "profile-images", "profile_");
                user.setProfilePhotoUrl(profilePictureUrl);
            }

            // Handle cover picture upload
            if (request.hasCoverPicture()) {
                String coverPictureUrl = uploadImage(request.getCoverPicture(), "cover-images", "cover_");
                user.setCoverPhotoUrl(coverPictureUrl);
            }

            // Save user
            user = userRepository.save(user);

            // Convert to response DTO
            UpdateProfileResponseDTO responseDTO = convertToResponseDTO(user);
            return ApiResponse.success(responseDTO, "Profile updated successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (IOException e) {
            return ApiResponse.error("Failed to upload image", "IMAGE_UPLOAD_FAILED");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while updating the profile", "INTERNAL_SERVER_ERROR");
        }
    }

    // Helper method to upload images
    private String uploadImage(MultipartFile image, String relativeDir, String prefix) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get("uploads/" + relativeDir + "/");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = prefix + UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save the image
        Files.copy(image.getInputStream(), filePath);

        // Generate URL
        return generateImageUrl(relativeDir, fileName);
    }

    // Generate image URL based on environment
    private String generateImageUrl(String relativeDir, String fileName) {
        return baseUrl + "/uploads/" + relativeDir + "/" + fileName;
    }

    // Helper method to convert User to UpdateProfileResponseDTO (same as UserDetailResponseDTO)
    private UpdateProfileResponseDTO convertToResponseDTO(User user) {
        UpdateProfileResponseDTO dto = new UpdateProfileResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
        dto.setCoverPhotoUrl(user.getCoverPhotoUrl());
        dto.setInstitutionalUser(user.isInstitutionalUser());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setDistrict(user.getDistrict());
        dto.setPalika(user.getPalika());
        dto.setWard(user.getWard());
        dto.setInstitutionCategory(user.getInstitutionCategory());
        dto.setVerified(user.isVerified());
        dto.setWebsite(user.getWebsite()); // Added website field
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}