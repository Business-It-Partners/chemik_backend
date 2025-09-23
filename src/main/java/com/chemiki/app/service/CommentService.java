package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateCommentRequestDTO;
import com.chemiki.app.dto.responseDto.CommentResponseDTO;
import com.chemiki.app.model.Comment;
import com.chemiki.app.model.Post;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.CommentRepository;
import com.chemiki.app.repository.PostRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService; // Added FCM service

    // Create a new comment
    public ApiResponse<CommentResponseDTO> createComment(CreateCommentRequestDTO request, Long userId) {
        try {
            // Validate user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate post exists and is active
            Post post = postRepository.findActivePostById(request.getPostId());
            if (post == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found or inactive");
            }

            // Create and save comment
            Comment comment = new Comment();
            comment.setPostId(request.getPostId());
            comment.setUserId(userId);
            comment.setContent(request.getContent());
            // Removed manual timestamp setting - @CreationTimestamp and @UpdateTimestamp will handle it
            comment.setDeleted(false);

            comment = commentRepository.save(comment);

            // Increment comment count on the post
            postRepository.incrementCommentCount(request.getPostId());

            // Send notification to post owner (if not commenting on own post)
            if (!post.getUserId().equals(userId)) {
                fcmService.sendCommentNotification(userId, post.getUserId(), request.getPostId(), request.getContent());
            }

            // Convert to response DTO
            CommentResponseDTO responseDTO = convertToResponseDTO(comment, user, userId);
            return ApiResponse.success(responseDTO, "Comment added successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "POST_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while adding the comment: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Get all comments for a post
    public ApiResponse<List<CommentResponseDTO>> getCommentsByPostId(Long postId, Long currentUserId) {
        try {
            // Validate post exists
            Post post = postRepository.findActivePostById(postId);
            if (post == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found or inactive");
            }

            List<Comment> comments = commentRepository.findCommentsByPostId(postId);
            List<CommentResponseDTO> responseDTOs = comments.stream()
                    .map(comment -> {
                        User user = userRepository.findById(comment.getUserId()).orElse(null);
                        return convertToResponseDTO(comment, user, currentUserId);
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "Comments retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "POST_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving comments", "INTERNAL_SERVER_ERROR");
        }
    }

    // Delete a comment (soft delete)
    public ApiResponse<Void> deleteComment(Long commentId, Long userId) {
        try {
            Comment comment = commentRepository.findActiveCommentById(commentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

            // Check if user owns this comment
            if (!comment.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this comment", "UNAUTHORIZED");
            }

            // Soft delete the comment
            comment.setDeleted(true);
            // @UpdateTimestamp will automatically update the updatedAt field
            commentRepository.save(comment);

            // Decrement comment count on the post
            postRepository.decrementCommentCount(comment.getPostId());

            return ApiResponse.success(null, "Comment deleted successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "COMMENT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the comment", "INTERNAL_SERVER_ERROR");
        }
    }

    // Helper method to convert Comment to CommentResponseDTO
    private CommentResponseDTO convertToResponseDTO(Comment comment, User user, Long currentUserId) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setUsername(user != null ? user.getUsername() : "Unknown User");
        dto.setUserProfilePicture(user != null ? user.getProfilePhotoUrl() : null);
        dto.setInstitutionalUser(user != null && user.isInstitutionalUser());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setTimeAgo(calculateTimeAgo(comment.getCreatedAt()));
        dto.setCanDelete(comment.getUserId().equals(currentUserId)); // Only comment owner can delete

        return dto;
    }

    // Helper method to calculate "time ago" - Updated for Instant
    private String calculateTimeAgo(Instant createdAt) {
        Instant now = Instant.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days < 7) return days + "d ago";

        // Convert to LocalDate for older dates
        LocalDate createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
        return createdDate.toString();
    }
}