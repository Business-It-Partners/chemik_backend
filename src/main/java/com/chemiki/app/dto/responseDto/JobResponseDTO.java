package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobResponseDTO {
    private Long id;
    private String title; // Changed from name to title
    private String description;
    private Double salary; // Changed from price to salary

    private LocalDateTime createdAt;
}