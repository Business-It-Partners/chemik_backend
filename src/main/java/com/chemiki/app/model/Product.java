package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category; // Remains String for flexibility

    @Column(nullable = false)
    private boolean isAvailable = true; // true = available, false = sold out

    @Column(nullable = false)
    private Long userId; // Foreign key referencing User.id

    @Column
    @ElementCollection
    private List<String> images; // List of image URLs

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private boolean deleted = false; // Soft delete flag
}