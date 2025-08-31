package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateProductRequestDTO;
import com.chemiki.app.dto.responseDto.ProductDetailResponseDTO;
import com.chemiki.app.dto.responseDto.ProductResponseDTO;
import com.chemiki.app.service.MarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // Create/Upload a new product
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @ModelAttribute CreateProductRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<ProductResponseDTO> response = marketplaceService.uploadProduct(request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "UNVERIFIED_USER":
                    status = HttpStatus.FORBIDDEN;
                    break;
                case "INVALID_INPUT":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case "IMAGE_UPLOAD_FAILED":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get all products (with optional category filter)
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllProducts(
            @RequestParam(required = false) String category) {
        ApiResponse<List<ProductResponseDTO>> response = marketplaceService.getAllProducts(category);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get single product details
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponseDTO>> getProductDetails(
            @PathVariable Long productId) {
        ApiResponse<ProductDetailResponseDTO> response = marketplaceService.getProductDetail(productId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Delete a product (soft delete)
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = marketplaceService.softDeleteProduct(productId, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Mark product as sold (set availability to false)
    @PatchMapping("/products/{productId}/mark-sold")
    public ResponseEntity<ApiResponse<Void>> markProductAsSold(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = marketplaceService.updateProductAvailability(productId, userId, false);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Mark product as available (set availability to true)
    @PatchMapping("/products/{productId}/mark-available")
    public ResponseEntity<ApiResponse<Void>> markProductAsAvailable(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = marketplaceService.updateProductAvailability(productId, userId, true);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get current user's products
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getUserProducts(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApiResponse<List<ProductResponseDTO>> response = marketplaceService.getUserProducts(userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("USER_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }


}