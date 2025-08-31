package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateProductRequestDTO;
import com.chemiki.app.dto.responseDto.ProductDetailResponseDTO;
import com.chemiki.app.dto.responseDto.ProductResponseDTO;
import com.chemiki.app.model.Product;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.ProductRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    // ✅ ADDED: Dynamic base URL configuration (same as PostService)
    @Value("${app.base-url:https://dgclick.com}")
    private String baseUrl;
    // Get server port from application.properties
    @Value("${server.port:8080}")
    private String serverPort;

    // Folder to store images (this will be served by Spring Boot)
    private static final String UPLOAD_DIR = "uploads/marketplace-images/";

    public ApiResponse<ProductResponseDTO> uploadProduct(CreateProductRequestDTO request, Long userId) {
        try {
            // Validate user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if institutional user is verified
            if (user.isInstitutionalUser() && !user.isVerified()) {
                return ApiResponse.error("Verification required for institutional users", "UNVERIFIED_USER");
            }

            // Validate input - updated with new fields
            if (request.getName() == null || request.getPrice() == null || request.getCategory() == null ||
                    request.getLocation() == null || request.getOwnerContact() == null || request.getCondition() == null ||
                    request.getImages() == null || request.getImages().isEmpty()) {
                return ApiResponse.error("All required fields (name, price, category, location, owner contact, condition) and at least one image are mandatory", "INVALID_INPUT");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Handle image uploads and generate URLs
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : request.getImages()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath); // Save the image

                // Generate correct URL that Spring Boot can serve
                String imageUrl = generateImageUrl(fileName);
                imageUrls.add(imageUrl);
            }

            // Create and save product with new fields
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setCategory(request.getCategory());
            product.setLocation(request.getLocation()); // New field
            product.setOwnerContact(request.getOwnerContact()); // New field
            product.setCondition(request.getCondition()); // New field
            product.setAvailable(true);
            product.setUserId(userId);
            product.setProductImageUrls(imageUrls);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            product = productRepository.save(product);

            // Map to response DTO with all fields
            ProductResponseDTO responseDTO = new ProductResponseDTO();
            responseDTO.setId(product.getId());
            responseDTO.setName(product.getName());
            responseDTO.setDescription(product.getDescription());
            responseDTO.setPrice(product.getPrice());
            responseDTO.setCategory(product.getCategory());
            responseDTO.setLocation(product.getLocation());
            responseDTO.setOwnerContact(product.getOwnerContact());
            responseDTO.setCondition(product.getCondition());
            responseDTO.setAvailable(product.isAvailable());
            responseDTO.setUserId(product.getUserId());
            responseDTO.setUsername(user.getUsername());
            responseDTO.setImages(product.getProductImageUrls());
            responseDTO.setCreatedAt(product.getCreatedAt());
            responseDTO.setUpdatedAt(product.getUpdatedAt());

            return ApiResponse.success(responseDTO, "Product uploaded successfully");
        } catch (IOException e) {
            return ApiResponse.error("Failed to upload images", "IMAGE_UPLOAD_FAILED");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while uploading the product", "INTERNAL_SERVER_ERROR");
        }
    }

    private String generateImageUrl(String fileName) {
        return baseUrl + "/uploads/marketplace-images/" + fileName;
    }
    public ApiResponse<List<ProductResponseDTO>> getAllProducts(String category) {
        try {
            List<Product> products;
            if (category != null && !category.trim().isEmpty()) {
                products = productRepository.findAllAvailableByCategoryOrderByCreatedAtDesc(category.trim());
                if (products.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No products found for category: " + category);
                }
            } else {
                products = productRepository.findAllAvailableOrderByCreatedAtDesc();
                if (products.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No products available");
                }
            }

            List<ProductResponseDTO> responseDTOs = products.stream().map(product -> {
                // Get user details for username
                User owner = userRepository.findById(product.getUserId()).orElse(null);

                ProductResponseDTO dto = new ProductResponseDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setDescription(product.getDescription());
                dto.setPrice(product.getPrice());
                dto.setCategory(product.getCategory());
                dto.setLocation(product.getLocation());
                dto.setOwnerContact(product.getOwnerContact());
                dto.setCondition(product.getCondition());
                dto.setAvailable(product.isAvailable());
                dto.setUserId(product.getUserId());
                dto.setUsername(owner != null ? owner.getUsername() : "Unknown");
                dto.setImages(product.getProductImageUrls());
                dto.setCreatedAt(product.getCreatedAt());
                dto.setUpdatedAt(product.getUpdatedAt());
                return dto;
            }).toList();
            return ApiResponse.success(responseDTOs, "Products retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "PRODUCT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving products", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<ProductDetailResponseDTO> getProductDetail(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .filter(p -> !p.isDeleted())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));

            User owner = userRepository.findById(product.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found for product id: " + productId));

            ProductDetailResponseDTO dto = new ProductDetailResponseDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setCategory(product.getCategory());
            dto.setLocation(product.getLocation());
            dto.setOwnerContact(product.getOwnerContact());
            dto.setCondition(product.getCondition());
            dto.setAvailable(product.isAvailable());
            dto.setUserId(product.getUserId());
            dto.setUsername(owner.getUsername());
             dto.setImages(product.getProductImageUrls());
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());

            return ApiResponse.success(dto, "Product details retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "PRODUCT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving product details", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<Void> softDeleteProduct(Long productId, Long userId) {
        try {
            Product product = productRepository.findByIdAndDeletedFalse(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));

            if (!product.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this product", "UNAUTHORIZED");
            }

            product.setDeleted(true);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            return ApiResponse.success(null, "Product soft deleted successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "PRODUCT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the product", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<Void> updateProductAvailability(Long productId, Long userId, boolean isAvailable) {
        try {
            Product product = productRepository.findByIdAndDeletedFalse(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));

            if (!product.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to update this product", "UNAUTHORIZED");
            }

            product.setAvailable(isAvailable);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            return ApiResponse.success(null, isAvailable ? "Product marked as available" : "Product marked as sold out");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "PRODUCT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while updating product availability", "INTERNAL_SERVER_ERROR");
        }
    }


    public ApiResponse<List<ProductResponseDTO>> getUserProducts(Long userId) {
        try {
            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found with id: " + userId, "USER_NOT_FOUND");
            }

            List<Product> products = productRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

            // If no products found, return empty list instead of error
            if (products.isEmpty()) {
                return ApiResponse.success(new ArrayList<>(), "No products found for user");
            }

            List<ProductResponseDTO> responseDTOs = products.stream().map(product -> {
                ProductResponseDTO dto = new ProductResponseDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setDescription(product.getDescription());
                dto.setPrice(product.getPrice());
                dto.setCategory(product.getCategory());
                dto.setLocation(product.getLocation());
                dto.setOwnerContact(product.getOwnerContact());
                dto.setCondition(product.getCondition());
                dto.setAvailable(product.isAvailable());
                dto.setUserId(product.getUserId());
                dto.setUsername(user.getUsername());
                dto.setImages(product.getProductImageUrls());
                dto.setCreatedAt(product.getCreatedAt());
                dto.setUpdatedAt(product.getUpdatedAt());
                return dto;
            }).toList();

            return ApiResponse.success(responseDTOs, "Products retrieved successfully for user: " + user.getUsername());
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving user products", "INTERNAL_SERVER_ERROR");
        }
    }

}