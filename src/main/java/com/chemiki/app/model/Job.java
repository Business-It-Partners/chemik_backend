package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Changed from name to title for job context

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double salary; // Changed from price to salary for job context

    @Column(nullable = false)
    private String category; // Remains String for flexibility (e.g., IT, Construction)

    @Column(nullable = false)
    private boolean isOpen = true; // Changed from isAvailable to isOpen (true = open, false = closed)

    @Column(nullable = false)
    private Long userId; // Foreign key referencing User.id (employer)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private boolean deleted = false; // Soft delete flag
}