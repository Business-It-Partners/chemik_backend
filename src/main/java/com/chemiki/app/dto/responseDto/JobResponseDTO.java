package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Double salary;
    private String category;
    private String location;
    private String contactNo;
    private String jobType;
    private boolean isOpen;
    private Long userId; // Owner's user ID
    private String username; // Owner's username
    private String profilePicture; // NEW: Profile picture URL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}