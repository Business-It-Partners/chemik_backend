package com.chemiki.app.repository;

import com.chemiki.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.isAvailable = true ORDER BY p.createdAt DESC")
    List<Product> findAllAvailableOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.isAvailable = true AND p.category = :category ORDER BY p.createdAt DESC")
    List<Product> findAllAvailableByCategoryOrderByCreatedAtDesc(String category);


    Optional<Product> findByIdAndDeletedFalse(Long id);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.userId = :userId ORDER BY p.createdAt DESC")
    List<Product> findAllByUserIdOrderByCreatedAtDesc(Long userId);

}