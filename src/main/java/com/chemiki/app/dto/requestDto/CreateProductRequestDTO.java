package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateProductRequestDTO {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description; // Optional

    @NotNull(message = "Price is required")
    private Double price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Location is required")
    private String location; // New field

    @NotBlank(message = "Owner contact is required")
    private String ownerContact; // New field

    @NotBlank(message = "Condition is required")
    private String condition; // New field (New, Like New, Good, Fair, Poor)

    @NotNull(message = "Images are required")
    private List<MultipartFile> images;
}