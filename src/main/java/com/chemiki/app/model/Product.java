package com.chemiki.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "marketplace_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Product name

    @Column(columnDefinition = "TEXT")
    private String description; // Product description

    @Column(nullable = false)
    private Double price; // Product price

    @Column(nullable = false)
    private String category; // Product category (e.g., "Electronics", "Clothing", "Books")

    @Column(nullable = false)
    private String location; // Product location (e.g., "Kathmandu", "Pokhara", "Chitwan")

    @Column(nullable = false)
    private String ownerContact; // Contact number for this specific product

    @Column(nullable = false)
    private String condition; // Product condition (e.g., "New", "Like New", "Good", "Fair", "Poor")

    @Column(nullable = false)
    private boolean isAvailable = true; // Product availability (true = available, false = sold out)

    @Column(nullable = false)
    private Long userId; // Foreign key referencing User.id (seller)

    @Column
    @ElementCollection
    private List<String> productImageUrls; // List of image URLs

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private boolean deleted = false; // Soft delete flag
}