package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobRequestDTO {
    @NotBlank(message = "Job title is required")
    private String title;

    @NotNull(message = "Salary is required")
    private Double salary;

    @NotBlank(message = "Job type is required")
    private String jobType; // Full-time, Part-time, Contract, Freelance, Internship

    private String description; // Optional

    @NotBlank(message = "Location is required")
    private String location; // Kathmandu, Pokhara, Remote, etc.

    @NotBlank(message = "Contact info is required")
    private String contactInfo; // Contact number for this job
}