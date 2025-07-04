package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDetailResponseDTO {
    private Long id;
    private String title; // Changed from name to title
    private String description;
    private Double salary; // Changed from price to salary
    private String category; // Kept but can be blank for now
    private LocalDateTime createdAt;
    private String ownerUsername;
    private String ownerPhoneNumber;
}