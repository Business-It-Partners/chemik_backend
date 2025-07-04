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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @PostMapping("/product")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> uploadProduct(
            @Valid @ModelAttribute CreateProductRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<ProductResponseDTO> response = marketplaceService.uploadProduct(request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
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
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }


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

    @GetMapping("/product/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponseDTO>> getProductDetail(
            @PathVariable Long id) {
        ApiResponse<ProductDetailResponseDTO> response = marketplaceService.getProductDetail(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }


//---------------------- to soft delete a product
    @DeleteMapping("/product/{id}")
    public ResponseEntity<ApiResponse<Void>> softDeleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = marketplaceService.softDeleteProduct(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }


    // --------------------------- to update product availability
    @PatchMapping("/product/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateProductAvailability(
            @PathVariable Long id,
            @RequestParam boolean isAvailable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = marketplaceService.updateProductAvailability(id, userId, isAvailable);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }


    @GetMapping("/my-products")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<ProductResponseDTO>> response = marketplaceService.getMyProducts(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("PRODUCT_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }
}