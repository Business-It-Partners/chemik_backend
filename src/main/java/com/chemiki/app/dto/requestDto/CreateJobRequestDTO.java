package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobRequestDTO {
    @NotBlank(message = "Job title is required")
    private String title; // Changed from name to title

    private String description;

    @NotNull(message = "Salary is required")
    private Double salary; // Changed from price to salary

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}