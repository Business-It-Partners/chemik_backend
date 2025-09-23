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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;

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

            post.setActive(true);
            post.setDeleted(false);

            Post savedPost = postRepository.save(post);

            // Send notification (unchanged from original)
            String notificationTitle = getNotificationTitle(savedPost.getPostType(),  user.isInstitutionalUser());
            fcmService.sendBroadcastNotification(userId, notificationTitle, user.getUsername()+ ": " +savedPost.getContent(), savedPost.getPostType(), savedPost.getId());

            PostResponseDTO responseDTO = convertToResponseDTO(savedPost, user, userId);
            return ApiResponse.success(responseDTO, "Post created successfully");
        } catch (IOException e) {
            return ApiResponse.error("Failed to upload images: " + e.getMessage(), "IMAGE_UPLOAD_FAILED");
        } catch (Exception e) {
            return ApiResponse.error("Failed to create post: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Get general posts (all except NEWS and NOTICE)
    public ApiResponse<Page<PostResponseDTO>> getGeneralPosts(Long currentUserId, int page, int size) {
        try {
            if (page < 0 || size <= 0) {
                return ApiResponse.error("Invalid page or size parameters", "INVALID_PAGINATION");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postPage = postRepository.findGeneralPosts(pageable);
            Page<PostResponseDTO> responsePage = postPage.map(post -> {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                return convertToResponseDTO(post, user, currentUserId);
            });
            return ApiResponse.success(responsePage, "General posts fetched successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch general posts: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Get news and notice posts
    public ApiResponse<Page<PostResponseDTO>> getNewsAndNoticePosts(Long currentUserId, int page, int size) {
        try {
            if (page < 0 || size <= 0) {
                return ApiResponse.error("Invalid page or size parameters", "INVALID_PAGINATION");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postPage = postRepository.findNewsAndNoticePosts(pageable);
            Page<PostResponseDTO> responsePage = postPage.map(post -> {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                return convertToResponseDTO(post, user, currentUserId);
            });
            return ApiResponse.success(responsePage, "News and notice posts fetched successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch news and notice posts: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Get user's own posts or specific user's posts
    public ApiResponse<Page<PostResponseDTO>> getUserPosts(Long userId, Long currentUserId, int page, int size) {
        try {
            if (page < 0 || size <= 0) {
                return ApiResponse.error("Invalid page or size parameters", "INVALID_PAGINATION");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postPage = postRepository.findUserPosts(userId, pageable);
            Page<PostResponseDTO> responsePage = postPage.map(post -> {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                return convertToResponseDTO(post, user, currentUserId);
            });
            return ApiResponse.success(responsePage, "User posts fetched successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch user posts: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }
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
    // Delete a post
    public ApiResponse<Void> deletePost(Long postId, Long userId) {
        try {
            Post post = postRepository.findActivePostById(postId);
            if (post == null) {
                return ApiResponse.error("Post not found", "POST_NOT_FOUND");
            }
            if (!post.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this post", "UNAUTHORIZED");
            }
            post.setDeleted(true);
             postRepository.save(post);
            return ApiResponse.success(null, "Post deleted successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete post: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Delete all posts (for testing)
    public ApiResponse<String> deleteAllPosts(Long userId) {
        try {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Fetch all posts in one go
            Page<Post> posts = postRepository.findUserPosts(userId, pageable);
            for (Post post : posts.getContent()) {
                post.setDeleted(true);
             }
            postRepository.saveAll(posts.getContent());
            return ApiResponse.success("All posts deleted successfully", "Posts deleted");
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete all posts: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    // Upload images
    private List<String> uploadImages(List<MultipartFile> images) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath);
            String imageUrl = generateImageUrl(fileName);
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    // Generate image URL
    private String generateImageUrl(String fileName) {
        return baseUrl + "/uploads/post-images/" + fileName;
    }

    // Convert Post to PostResponseDTO
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
        dto.setTimeAgo( "");
        dto.setHasViewedByCurrentUser(postViewRepository.existsByPostIdAndUserId(post.getId(), currentUserId));
        return dto;
    }

    // Calculate time ago

    // Helper method for notifications
    private String getNotificationTitle(String postType,   boolean isInstitutionalUser) {

        switch (postType) {
            case "NEWS":
                return " New Update"  ;
            case "NOTICE":
                return "📢 Notice"  ;
            case "ALERT":
                return "🚨 Urgent Alert" ;
            case "LOST_AND_FOUND":
                return "🔍 Lost & Found" ;
            default:
                return "New Post"  ;
        }
    }
}