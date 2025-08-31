package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreatePostRequestDTO;
import com.chemiki.app.dto.responseDto.PostResponseDTO;
import com.chemiki.app.model.Post;
import com.chemiki.app.model.PostView;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.PostRepository;
import com.chemiki.app.repository.PostViewRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService; // 🔥 NEW: Added FCM service

    // Dynamic base URL configuration
    @Value("${app.base-url:https://dgclick.com}")
    private String baseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String UPLOAD_DIR = "uploads/post-images/";
    private static final List<String> VALID_POST_TYPES = Arrays.asList(
            "GENERAL", "NEWS", "NOTICE", "ALERT", "LOST_AND_FOUND"
    );

    // Create a new post
    public ApiResponse<PostResponseDTO> createPost(CreatePostRequestDTO request, Long userId) {
        try {
            // Validate user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate post type
            if (!VALID_POST_TYPES.contains(request.getPostType())) {
                return ApiResponse.error("Invalid post type. Allowed: " + String.join(", ", VALID_POST_TYPES), "INVALID_POST_TYPE");
            }

            // Validate institutional user for NEWS and NOTICE
            if (Arrays.asList("NEWS", "NOTICE").contains(request.getPostType()) && !user.isInstitutionalUser()) {
                return ApiResponse.error("Only institutional users can create NEWS and NOTICE posts", "UNAUTHORIZED_POST_TYPE");
            }

            // Handle image uploads if present
            List<String> imageUrls = new ArrayList<>();
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                imageUrls = uploadImages(request.getImages());
            }

            // Create and save post
            Post post = new Post();
            post.setUserId(userId);
            post.setContent(request.getContent());
            post.setPostType(request.getPostType());
            post.setImagesUrls(imageUrls);
            post.setLink(request.getLink());
            post.setViewCount(0L);
            post.setCommentCount(0L);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post.setActive(true);
            post.setDeleted(false);

            post = postRepository.save(post);

            // 🔥 NEW: Send notifications for specific post types
             if (Arrays.asList("NEWS", "NOTICE", "ALERT", "LOST_AND_FOUND").contains(request.getPostType())) {
                String title = getNotificationTitle(request.getPostType(), user.getUsername(), user.isInstitutionalUser());
                String body = request.getContent().length() > 100 ? request.getContent().substring(0, 100) + "..." : request.getContent();

                // Send broadcast notification asynchronously
                fcmService.sendBroadcastNotification(userId, title, body, request.getPostType(), post.getId());
            }


            // Convert to response DTO
            PostResponseDTO responseDTO = convertToResponseDTO(post, user, userId);
            return ApiResponse.success(responseDTO, "Post created successfully");

        } catch (Exception e) {
            return ApiResponse.error("An error occurred while creating the post: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Get general posts (all except NEWS and NOTICE)
    public ApiResponse<List<PostResponseDTO>> getGeneralPosts(Long currentUserId) {
        try {
            List<Post> posts = postRepository.findGeneralPosts();
            List<PostResponseDTO> responseDTOs = posts.stream()
                    .map(post -> {
                        User user = userRepository.findById(post.getUserId()).orElse(null);
                        return convertToResponseDTO(post, user, currentUserId);
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "General posts retrieved successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving general posts", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get news and notice posts (only from institutional users)
    public ApiResponse<List<PostResponseDTO>> getNewsAndNoticePosts(Long currentUserId) {
        try {
            List<Post> posts = postRepository.findNewsAndNoticePosts();
            List<PostResponseDTO> responseDTOs = posts.stream()
                    .map(post -> {
                        User user = userRepository.findById(post.getUserId()).orElse(null);
                        return convertToResponseDTO(post, user, currentUserId);
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "News and notice posts retrieved successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving news and notice posts", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get single post and increment view count
    public ApiResponse<PostResponseDTO> getPost(Long postId, Long currentUserId) {
        try {
            Post post = postRepository.findActivePostById(postId);
            if (post == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
            }

            User user = userRepository.findById(post.getUserId()).orElse(null);

            // Record view if user hasn't viewed this post before
            if (!postViewRepository.existsByPostIdAndUserId(postId, currentUserId)) {
                PostView postView = new PostView();
                postView.setPostId(postId);
                postView.setUserId(currentUserId);
                postView.setViewedAt(LocalDateTime.now());
                postViewRepository.save(postView);

                // Increment view count in post
                postRepository.incrementViewCount(postId);
                post.setViewCount(post.getViewCount() + 1);
            }

            PostResponseDTO responseDTO = convertToResponseDTO(post, user, currentUserId);
            return ApiResponse.success(responseDTO, "Post retrieved successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "POST_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving the post", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get user's own posts
    public ApiResponse<List<PostResponseDTO>> getUserPosts(Long userId, Long currentUserId) {
        try {
            List<Post> posts = postRepository.findUserPosts(userId);
            User user = userRepository.findById(userId).orElse(null);

            List<PostResponseDTO> responseDTOs = posts.stream()
                    .map(post -> convertToResponseDTO(post, user, currentUserId))
                    .collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "User posts retrieved successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving user posts", "INTERNAL_SERVER_ERROR");
        }
    }

    // Soft delete a post (only post owner can delete)
    public ApiResponse<Void> deletePost(Long postId, Long userId) {
        try {
            Post post = postRepository.findActivePostById(postId);
            if (post == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
            }

            // Check if user owns this post
            if (!post.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this post", "UNAUTHORIZED");
            }

            // Soft delete the post
            post.setDeleted(true);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);

            return ApiResponse.success(null, "Post deleted successfully");

        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "POST_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the post", "INTERNAL_SERVER_ERROR");
        }
    }

    // DELETE ALL POSTS - For development/testing purposes
    public ApiResponse<String> deleteAllPosts(Long requestingUserId) {
        try {
            // Get user to check if they're institutional (optional security check)
            User user = userRepository.findById(requestingUserId).orElse(null);

            // Count posts before deletion
            List<Post> allPosts = postRepository.findAll();
            int totalPosts = allPosts.size();

            if (totalPosts == 0) {
                return ApiResponse.success("No posts found to delete", "No posts found to delete");
            }

            // Delete all posts (hard delete - be careful!)
            postRepository.deleteAll();

            // Also delete all post views and comments related to these posts
            postViewRepository.deleteAll();
            // Note: If you have comment repository, add: commentRepository.deleteAll();

            String message = String.format("Successfully deleted %d posts and all related data", totalPosts);
            return ApiResponse.success(message, message);

        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting all posts: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Helper method to upload images with dynamic base URL
    private List<String> uploadImages(List<MultipartFile> images) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath);

            // Dynamic URL generation based on environment
            String imageUrl = generateImageUrl(fileName);
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    // Generate image URL based on environment
    private String generateImageUrl(String fileName) {
        // Use the configured base URL or fallback to localhost for development
        return baseUrl + "/uploads/post-images/" + fileName;
    }

    // Helper method to convert Post to PostResponseDTO
    private PostResponseDTO convertToResponseDTO(Post post, User user, Long currentUserId) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setUsername(user != null ? user.getUsername() : "Unknown User");
        dto.setUserProfilePicture(user != null ? user.getProfilePhotoUrl() : null);
        dto.setInstitutionalUser(user != null && user.isInstitutionalUser());
        dto.setContent(post.getContent());
        dto.setImages(post.getImagesUrls());
        dto.setPostType(post.getPostType());
        dto.setViewCount(post.getViewCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setLink(post.getLink());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setActive(post.isActive());
        dto.setTimeAgo(calculateTimeAgo(post.getCreatedAt()));
        dto.setHasViewedByCurrentUser(postViewRepository.existsByPostIdAndUserId(post.getId(), currentUserId));

        return dto;
    }

    // Helper method to calculate "time ago"
    private String calculateTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days < 7) return days + "d ago";
        return createdAt.toLocalDate().toString();
    }

    // 🔥 NEW: Helper methods for notifications
    // Helper method for notifications
    private String getNotificationTitle(String postType, String authorName, boolean isInstitutionalUser) {
        String source = isInstitutionalUser ? authorName : authorName + "'s Post";
        switch (postType) {
            case "NEWS":
                return "📰 New Update: " + source;
            case "NOTICE":
                return "📢 Notice: " + source;
            case "ALERT":
                return "🚨 Urgent Alert: " + source;
            case "LOST_AND_FOUND":
                return "🔍 Lost & Found: " + source;
            default:
                return "New Post: " + source;
        }
    }

}