package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.UpdateProfileRequestDTO;
import com.chemiki.app.dto.responseDto.UpdateProfileResponseDTO;
import com.chemiki.app.service.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileUpdateController {

    private final ProfileUpdateService profileUpdateService;

    // Update profile with profile picture (multipart/form-data)
    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UpdateProfileResponseDTO>> updateProfileWithImage(
            @ModelAttribute UpdateProfileRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<UpdateProfileResponseDTO> response = profileUpdateService.updateProfile(request, userId);
        return handleUpdateProfileResponse(response);
    }

    // Update profile without images (application/json)
    @PutMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<UpdateProfileResponseDTO>> updateProfileJson(
            @RequestBody UpdateProfileRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<UpdateProfileResponseDTO> response = profileUpdateService.updateProfile(request, userId);
        return handleUpdateProfileResponse(response);
    }

    // Helper method to handle response logic (DRY principle)
    private ResponseEntity<ApiResponse<UpdateProfileResponseDTO>> handleUpdateProfileResponse(
            ApiResponse<UpdateProfileResponseDTO> response) {
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "USERNAME_EXISTS":
                    status = HttpStatus.CONFLICT;
                    break;
                case "IMAGE_UPLOAD_FAILED":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }
}