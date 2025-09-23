package com.chemiki.app.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class JobDetailResponseDTO {
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
    private String ownerPhoneNumber; // Owner's phone number
    private String profilePicture; // NEW: Profile picture URL

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}