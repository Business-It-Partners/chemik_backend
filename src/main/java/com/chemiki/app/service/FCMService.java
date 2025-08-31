package com.chemiki.app.service;

import com.chemiki.app.dto.requestDto.NotificationRequestDTO;
import com.chemiki.app.dto.responseDto.NotificationResponseDTO;
import com.chemiki.app.model.DeviceToken;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final DeviceTokenService deviceTokenService;
    private final UserRepository userRepository;

    /**
     * Legacy method for backward compatibility
     */
    public NotificationResponseDTO sendNotification(NotificationRequestDTO request) throws FirebaseMessagingException {
        try {
            Message message = Message.builder()
                    .setToken(request.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .putData("key1", "value1") // Optional custom data
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            return new NotificationResponseDTO(messageId, true, "Notification sent successfully");
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Send notification to a single user (for messages, comments)
     */
    @Async
    public void sendNotificationToUser(Long senderId, Long receiverId, String title, String body,
                                       String notificationType, Long relatedEntityId) {
        try {
            // Don't send notification to self
            if (senderId.equals(receiverId)) {
                return;
            }

            // Get active tokens for receiver
            List<DeviceToken> receiverTokens = deviceTokenService.getActiveTokensForUser(receiverId);

            if (receiverTokens.isEmpty()) {
                log.warn("No active tokens found for user {}", receiverId);
                return;
            }

            // Create enhanced data payload
            Map<String, String> data = createEnhancedDataPayload(notificationType, senderId, relatedEntityId);

            // Send to all user's devices
            int successCount = 0;
            for (DeviceToken token : receiverTokens) {
                try {
                    String messageId = sendFCMMessage(token.getFcmToken(), title, body, data);
                    if (messageId != null) {
                        successCount++;
                        deviceTokenService.markTokenAsUsed(token.getFcmToken());
                        log.debug("Notification sent successfully to token: {}...",
                                token.getFcmToken().substring(0, Math.min(token.getFcmToken().length(), 0)));
                    }
                } catch (FirebaseMessagingException e) {
                    log.error("Failed to send notification to token {}: {}",
                            token.getFcmToken().substring(0, Math.min(token.getFcmToken().length(), 20)), e.getMessage());

                    // Mark token as inactive if it's invalid
                    if (isTokenInvalid(e)) {
                        deviceTokenService.markTokenAsInactive(token.getFcmToken());
                    }
                }
            }

            log.info("Notification sent to user {}. Success: {}/{}", receiverId, successCount, receiverTokens.size());

        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", receiverId, e.getMessage());
        }
    }

    /**
     * Send broadcast notification to all users except sender (for NEWS, NOTICE, ALERT, LOST_AND_FOUND posts)
     */
    @Async
    public void sendBroadcastNotification(Long senderId, String title, String body, String postType, Long postId) {
        try {
            // Validate postType
            List<String> validPostTypes = List.of("NEWS", "NOTICE", "ALERT", "LOST_AND_FOUND");
            if (!validPostTypes.contains(postType)) {
                log.error("Invalid post type: {}", postType);
                return;
            }

            // Get sender info for navigation
            User sender = userRepository.findById(senderId).orElse(null);

            // Create enhanced data payload for post navigation
            Map<String, String> data = new HashMap<>();
            data.put("type", "POST");
            data.put("action", "OPEN_POST");
            data.put("postType", postType);
            data.put("postId", String.valueOf(postId));
            data.put("senderId", String.valueOf(senderId));
            if (sender != null) {
                data.put("senderUsername", sender.getUsername());
                data.put("isInstitutional", String.valueOf(sender.isInstitutionalUser()));
            }
            data.put("navigateTo", "PostDetailScreen");
            data.put("isFromNotification", "true");
            data.put("click_action", "OPEN_POST");

            // Build message for the "posts" topic
            Message message = Message.builder()
                    .setTopic("posts") // Send to "posts" topic
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(AndroidNotification.builder()
                                    .setClickAction("OPEN_POST")
                                    .setChannelId("posts")
                                    .setSound("default")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setCategory("POST_NOTIFICATION")
                                    .build())
                            .build())
                    .build();

            // Send the message
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("Broadcast notification sent for {} post. Message ID: {}", postType, messageId);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending broadcast notification to posts topic: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending broadcast notification: {}", e.getMessage());
        }
    }

    /**
     * Send message notification with enhanced navigation data
     */
    @Async
    public void sendMessageNotification(Long senderId, Long receiverId, String messageContent) {
        try {
            User sender = userRepository.findById(senderId).orElse(null);
            if (sender == null) {
                log.warn("Sender user {} not found for message notification", senderId);
                return;
            }

            String title = "💬 " + sender.getUsername() + " sent you a message";
            String body = messageContent.length() > 80 ?
                    messageContent.substring(0, 80) + "..." : messageContent;

            // Create enhanced data payload for message navigation
            Map<String, String> data = new HashMap<>();
            data.put("type", "MESSAGE");
            data.put("action", "OPEN_CHAT");
            data.put("senderId", String.valueOf(senderId));
            data.put("senderUsername", sender.getUsername());
            data.put("receiverId", String.valueOf(receiverId));
            data.put("navigateTo", "ChatScreen");
            data.put("chatUserId", String.valueOf(senderId)); // User to open chat with
            data.put("isFromNotification", "true");
            data.put("click_action", "OPEN_CHAT");

            sendNotificationToUserWithData(senderId, receiverId, title, body, data);
        } catch (Exception e) {
            log.error("Error sending message notification: {}", e.getMessage());
        }
    }

    /**
     * Send comment notification with enhanced navigation data
     */
    @Async
    public void sendCommentNotification(Long commenterId, Long postOwnerId, Long postId, String commentContent) {
        try {
            User commenter = userRepository.findById(commenterId).orElse(null);
            if (commenter == null) {
                log.warn("Commenter user {} not found for comment notification", commenterId);
                return;
            }

            String title = "💬 " + commenter.getUsername() + " commented on your post";
            String body = commentContent.length() > 80 ?
                    commentContent.substring(0, 80) + "..." : commentContent;

            // Create enhanced data payload for comment navigation
            Map<String, String> data = new HashMap<>();
            data.put("type", "COMMENT");
            data.put("action", "OPEN_POST");
            data.put("commenterId", String.valueOf(commenterId));
            data.put("commenterUsername", commenter.getUsername());
            data.put("postId", String.valueOf(postId));
            data.put("postOwnerId", String.valueOf(postOwnerId));
            data.put("navigateTo", "PostDetailScreen");
            data.put("highlightComment", "true");
            data.put("scrollToComments", "true");
            data.put("isFromNotification", "true");
            data.put("click_action", "OPEN_POST");

            sendNotificationToUserWithData(commenterId, postOwnerId, title, body, data);
        } catch (Exception e) {
            log.error("Error sending comment notification: {}", e.getMessage());
        }
    }

    /**
     * Helper method to send notification with custom data payload
     */
    private void sendNotificationToUserWithData(Long senderId, Long receiverId, String title, String body, Map<String, String> data) {
        try {
            // Don't send notification to self
            if (senderId.equals(receiverId)) {
                return;
            }

            // Get active tokens for receiver
            List<DeviceToken> receiverTokens = deviceTokenService.getActiveTokensForUser(receiverId);

            if (receiverTokens.isEmpty()) {
                log.warn("No active tokens found for user {}", receiverId);
                return;
            }

            // Send to all user's devices
            int successCount = 0;
            for (DeviceToken token : receiverTokens) {
                try {
                    String messageId = sendFCMMessage(token.getFcmToken(), title, body, data);
                    if (messageId != null) {
                        successCount++;
                        deviceTokenService.markTokenAsUsed(token.getFcmToken());
                        log.debug("Notification sent successfully to token: {}...",
                                token.getFcmToken().substring(0, Math.min(token.getFcmToken().length(), 20)));
                    }
                } catch (FirebaseMessagingException e) {
                    log.error("Failed to send notification to token {}: {}",
                            token.getFcmToken().substring(0, Math.min(token.getFcmToken().length(), 20)), e.getMessage());

                    // Mark token as inactive if it's invalid
                    if (isTokenInvalid(e)) {
                        deviceTokenService.markTokenAsInactive(token.getFcmToken());
                    }
                }
            }

            log.info("Notification sent to user {}. Success: {}/{}", receiverId, successCount, receiverTokens.size());

        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", receiverId, e.getMessage());
        }
    }

    /**
     * Helper method to send FCM message
     */
    private String sendFCMMessage(String token, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setClickAction("OPEN_APP")
                                .setSound("default")
                                .setChannelId("default")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    /**
     * Helper method to create enhanced data payload
     */
    private Map<String, String> createEnhancedDataPayload(String notificationType, Long senderId, Long relatedEntityId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", notificationType);
        data.put("senderId", String.valueOf(senderId));
        data.put("isFromNotification", "true");

        // Get sender info for enhanced data
        User sender = userRepository.findById(senderId).orElse(null);
        if (sender != null) {
            data.put("senderUsername", sender.getUsername());
        }

        if (relatedEntityId != null) {
            data.put("relatedEntityId", String.valueOf(relatedEntityId));

            // Add specific data based on notification type
            switch (notificationType) {
                case "MESSAGE":
                    data.put("action", "OPEN_CHAT");
                    data.put("navigateTo", "ChatScreen");
                    data.put("chatUserId", String.valueOf(senderId));
                    data.put("click_action", "OPEN_CHAT");
                    break;
                case "COMMENT":
                    data.put("action", "OPEN_POST");
                    data.put("navigateTo", "PostDetailScreen");
                    data.put("postId", String.valueOf(relatedEntityId));
                    data.put("scrollToComments", "true");
                    data.put("click_action", "OPEN_POST");
                    break;
                default:
                    data.put("click_action", "OPEN_APP");
                    break;
            }
        } else {
            data.put("click_action", "OPEN_APP");
        }

        return data;
    }

    /**
     * Helper method to check if token error indicates invalid token
     */
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        try {
            // Check error code if available
            if (e.getErrorCode() != null) {
                String errorCode = e.getErrorCode().toString(); // Convert to string safely
                return "UNREGISTERED".equals(errorCode) ||
                        "INVALID_ARGUMENT".equals(errorCode);
            }

            // Check error message for common invalid token patterns
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                return errorMessage.contains("not a valid FCM registration token") ||
                        errorMessage.contains("Requested entity was not found") ||
                        errorMessage.contains("registration token is not a valid FCM") ||
                        errorMessage.toLowerCase().contains("unregistered") ||
                        errorMessage.toLowerCase().contains("invalid");
            }

            return false;
        } catch (Exception ex) {
            // If any error occurs while checking, log it and return false
            log.warn("Error while checking if token is invalid: {}", ex.getMessage());
            return false;
        }
    }
}