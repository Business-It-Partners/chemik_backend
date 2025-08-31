package com.chemiki.app.dto.responseDto;

import lombok.Data;
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
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}