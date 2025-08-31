package com.chemiki.app.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {
    @NotBlank(message = "Post content is required")
    private String content;

    @NotNull(message = "Post type is required")
    private String postType; // GENERAL, NEWS, NOTICE, ALERT, LOST_AND_FOUND

    // For multipart/form-data requests (when uploading images)
    @JsonIgnore // This will be ignored in JSON requests
    private List<MultipartFile> images; // Optional images

    private String link; // Optional: Link to product/job if related

    // Helper method to check if request has images
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
}