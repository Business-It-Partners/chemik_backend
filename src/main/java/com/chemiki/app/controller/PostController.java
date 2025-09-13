package com.chemiki.app.controller;

import com.chemiki.app.config.CustomUserDetails;
import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreatePostRequestDTO;
import com.chemiki.app.dto.responseDto.PostResponseDTO;
import com.chemiki.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // Create a new post with images (multipart/form-data)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPostWithImages(
            @Valid @ModelAttribute CreatePostRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<PostResponseDTO> response = postService.createPost(request, userId);
        return handleCreatePostResponse(response);
    }

    // Create a new post without images (application/json)
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPostJson(
            @Valid @RequestBody CreatePostRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<PostResponseDTO> response = postService.createPost(request, userId);
        return handleCreatePostResponse(response);
    }

    // Helper method to handle response logic (DRY principle)
    private ResponseEntity<ApiResponse<PostResponseDTO>> handleCreatePostResponse(ApiResponse<PostResponseDTO> response) {
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "INVALID_POST_TYPE":
                case "UNAUTHORIZED_POST_TYPE":
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

    // Get general posts (all except NEWS and NOTICE) - For "General" tab
    @GetMapping("/general")
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getGeneralPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Page<PostResponseDTO>> response = postService.getGeneralPosts(currentUserId, page, size);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get news and notice posts - For "News & Notice" tab
    @GetMapping("/news-notice")
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getNewsAndNoticePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Page<PostResponseDTO>> response = postService.getNewsAndNoticePosts(currentUserId, page, size);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<PostResponseDTO> response = postService.getPost(postId, currentUserId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("POST_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }
    // Get current user's posts (own profile view)
    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Page<PostResponseDTO>> response = postService.getUserPosts(userId, userId, page, size);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get specific user's posts (public profile view)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Page<PostResponseDTO>> response = postService.getUserPosts(userId, currentUserId, page, size);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Soft delete a post (only post owner can delete)
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = postService.deletePost(postId, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("POST_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Toggle post active status (only post owner can toggle)
    @PatchMapping("/{postId}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> togglePostStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(null, "Toggle status functionality coming soon"));
    }


}