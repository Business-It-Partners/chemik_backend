package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String location; // New field
    private String ownerContact; // New field - product-specific contact
    private String condition; // New field
    private boolean isAvailable;
    private Long userId; // Owner's user ID
    private String username; // Owner's username
    private String profilePicture;
    private List<String> images;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}