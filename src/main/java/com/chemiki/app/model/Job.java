package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Job title (e.g., "Software Developer", "Marketing Manager")

    @Column(columnDefinition = "TEXT")
    private String description; // Job description

    @Column(nullable = false)
    private Double salary; // Salary amount

    @Column(nullable = false)
    private String category; // Job category (e.g., "IT", "Construction", "Healthcare")

    @Column(nullable = false)
    private String location; // Job location (e.g., "Kathmandu", "Pokhara", "Remote")

    @Column(nullable = false)
    private String contactNo; // Contact number for this specific job

    @Column(nullable = false)
    private String jobType; // Job type (e.g., "Full-time", "Part-time", "Contract", "Freelance", "Internship")

    @Column(nullable = false)
    private boolean isOpen = true; // Job status (true = open, false = closed)

    @Column(nullable = false)
    private Long userId; // Foreign key referencing User.id (employer)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private boolean deleted = false; // Soft delete flag
}