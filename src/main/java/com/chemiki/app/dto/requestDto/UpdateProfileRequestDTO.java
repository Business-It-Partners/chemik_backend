package com.chemiki.app.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileRequestDTO {
    private String username;
    private String email;
    private String website;

    // For profile picture upload (multipart/form-data requests)
    @JsonIgnore // This will be ignored in JSON requests
    private MultipartFile profilePicture;

    // Helper method to check if request has profile picture
    public boolean hasProfilePicture() {
        return profilePicture != null && !profilePicture.isEmpty();
    }
}