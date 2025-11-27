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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final RestTemplate restTemplate; // Injected from config

    private static final String HF_API_URL = "https://abishektiwari-nepali-offensive-detector.hf.space/detect";

    // ============== OFFENSIVE CONTENT CHECK ==============
    private boolean isOffensiveNepali(String text) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(HF_API_URL, entity, Map.class);
            Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");

            if (result == null || !(Boolean) result.get("success")) {
                return false; // Fail open if API down
            }

            boolean isOffensive = (Boolean) result.get("is_offensive");
            double confidence = ((Number) result.get("confidence")).doubleValue();

            return isOffensive && confidence > 0.7;

        } catch (Exception e) {
            System.err.println("HF API Error: " + e.getMessage());
            return false; // Fail open — don't block user
        }
    }

    // ============== CREATE COMMENT ==============
    public ApiResponse<CommentResponseDTO> createComment(CreateCommentRequestDTO request, Long userId) {
        try {
            // Validate user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate post
            Post post = postRepository.findActivePostById(request.getPostId());
            if (post == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found or inactive");
            }

            String content = request.getContent();

//            // OFFENSIVE CONTENT FILTER
//            if (isOffensiveNepali(content)) {
//                return ApiResponse.error(
//                        "राम्रो नागरिक बनौं र सदैव सकारात्मक कमेन्ट गरौं।",
//                        "OFFENSIVE_CONTENT"
//                );
//            }

            // Create comment
            Comment comment = new Comment();
            comment.setPostId(request.getPostId());
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setDeleted(false);

            comment = commentRepository.save(comment);

            // Update post comment count
            postRepository.incrementCommentCount(request.getPostId());

            // Send FCM notification
            if (!post.getUserId().equals(userId)) {
                fcmService.sendCommentNotification(userId, post.getUserId(), request.getPostId(), content);
            }

            // Return response
            CommentResponseDTO responseDTO = convertToResponseDTO(comment, user, userId);
            return ApiResponse.success(responseDTO, "Comment added successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "POST_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // ============== GET COMMENTS ==============
    public ApiResponse<List<CommentResponseDTO>> getCommentsByPostId(Long postId, Long currentUserId) {
        try {
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

    // ============== DELETE COMMENT ==============
    public ApiResponse<Void> deleteComment(Long commentId, Long userId) {
        try {
            Comment comment = commentRepository.findActiveCommentById(commentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

            if (!comment.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this comment", "UNAUTHORIZED");
            }

            comment.setDeleted(true);
            commentRepository.save(comment);
            postRepository.decrementCommentCount(comment.getPostId());

            return ApiResponse.success(null, "Comment deleted successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "COMMENT_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the comment", "INTERNAL_SERVER_ERROR");
        }
    }

    // ============== HELPER: DTO CONVERSION ==============
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
        dto.setCanDelete(comment.getUserId().equals(currentUserId));
        return dto;
    }

    // ============== HELPER: TIME AGO ==============
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

        LocalDate createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
        return createdDate.toString();
    }
}