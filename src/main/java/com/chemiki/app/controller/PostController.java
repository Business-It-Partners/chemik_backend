package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreatePostRequestDTO;
import com.chemiki.app.dto.responseDto.PostResponseDTO;
import com.chemiki.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<PostResponseDTO> response = postService.createPost(request, userId);
        return handleCreatePostResponse(response);
    }

    // Create a new post without images (application/json)
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPostJson(
            @Valid @RequestBody CreatePostRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
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
    public ResponseEntity<ApiResponse<List<PostResponseDTO>>> getGeneralPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<PostResponseDTO>> response = postService.getGeneralPosts(currentUserId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get news and notice posts (only from institutional users) - For "News & Notice" tab
    @GetMapping("/news-notice")
    public ResponseEntity<ApiResponse<List<PostResponseDTO>>> getNewsAndNoticePosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<PostResponseDTO>> response = postService.getNewsAndNoticePosts(currentUserId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get single post by ID (also increments view count)
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

    // Get current user's posts
    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostResponseDTO>>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<PostResponseDTO>> response = postService.getUserPosts(userId, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get specific user's posts (public profile view)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PostResponseDTO>>> getUserPosts(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<PostResponseDTO>> response = postService.getUserPosts(userId, currentUserId);
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
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
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
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        // You can implement this in PostService later
        return ResponseEntity.ok(ApiResponse.success(null, "Toggle status functionality coming soon"));
    }



    // DELETE ALL POSTS - For development/testing purposes only
    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<String> response = postService.deleteAllPosts(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}