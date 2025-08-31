package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateCommentRequestDTO;
import com.chemiki.app.dto.responseDto.CommentResponseDTO;
import com.chemiki.app.service.ClaudeService;
import com.chemiki.app.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;

//  to create a comment
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @Valid @RequestBody CreateCommentRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<CommentResponseDTO> response = commentService.createComment(request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("POST_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get all comments for a specific post
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getCommentsByPostId(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<CommentResponseDTO>> response = commentService.getCommentsByPostId(postId, currentUserId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("POST_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Delete a comment (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = commentService.deleteComment(commentId, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("COMMENT_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

}