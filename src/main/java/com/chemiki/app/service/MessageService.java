package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.SendMessageRequestDTO;
import com.chemiki.app.dto.responseDto.ConversationResponseDTO;
import com.chemiki.app.dto.responseDto.MessageResponseDTO;
import com.chemiki.app.model.Message;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.MessageRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService; // 🔥 NEW: Added FCM service

    // Send a message
    public ApiResponse<MessageResponseDTO> sendMessage(SendMessageRequestDTO request, Long senderId) {
        try {
            // Validate sender
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            // Validate receiver
            User receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

            // Check if trying to send message to self
            if (senderId.equals(request.getReceiverId())) {
                return ApiResponse.error("Cannot send message to yourself", "INVALID_RECEIVER");
            }

            // Create and save message
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(request.getReceiverId());
            message.setContent(request.getContent());
            message.setRead(false);
            // Removed manual setCreatedAt - @CreationTimestamp will handle it automatically
            message = messageRepository.save(message);

            // 🔥 NEW: Send push notification for new message
            fcmService.sendMessageNotification(senderId, request.getReceiverId(), request.getContent());

            // Create response DTO
            MessageResponseDTO responseDTO = new MessageResponseDTO();
            responseDTO.setId(message.getId());
            responseDTO.setSenderId(message.getSenderId());
            responseDTO.setSenderUsername(sender.getUsername());
            responseDTO.setReceiverId(message.getReceiverId());
            responseDTO.setReceiverUsername(receiver.getUsername());
            responseDTO.setContent(message.getContent());
            responseDTO.setRead(message.isRead());
            responseDTO.setCreatedAt(message.getCreatedAt());
            responseDTO.setReadAt(message.getReadAt());

            return ApiResponse.success(responseDTO, "Message sent successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while sending the message", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get conversation between two users
    public ApiResponse<List<MessageResponseDTO>> getConversation(Long userId, Long otherUserId) {
        try {
            // Validate users exist
            userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            userRepository.findById(otherUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Other user not found"));

            List<Message> messages = messageRepository.findConversationBetweenUsers(userId, otherUserId);

            // Mark messages as read for the current user
            markMessagesAsRead(userId, otherUserId);

            List<MessageResponseDTO> responseDTOs = messages.stream().map(message -> {
                User sender = userRepository.findById(userId).orElse(null);
                User receiver = userRepository.findById(otherUserId ).orElse(null);

                MessageResponseDTO dto = new MessageResponseDTO();
                dto.setId(message.getId());
                dto.setSenderId(message.getSenderId());
                dto.setSenderUsername(sender != null ? sender.getUsername() : "Unknown");
                dto.setReceiverId(message.getReceiverId());
                dto.setReceiverUsername(receiver != null ? receiver.getUsername() : "Unknown");
                dto.setContent(message.getContent());
                dto.setRead(message.isRead());
                dto.setCreatedAt(message.getCreatedAt());
                dto.setReadAt(message.getReadAt());
                dto.setSenderProfilePicture( sender.getProfilePhotoUrl() );
                dto.setReceiverProfilePicture( receiver.getProfilePhotoUrl() );

                return dto;
            }).collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "Conversation retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving the conversation", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get all conversations for a user
    public ApiResponse<List<ConversationResponseDTO>> getUserConversations(Long userId) {
        try {
            userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            List<Message> latestMessages = messageRepository.findLatestConversationsForUser(userId);

            List<ConversationResponseDTO> responseDTOs = new ArrayList<>();
            Map<Long, ConversationResponseDTO> conversationMap = new HashMap<>();

            for (Message message : latestMessages) {
                Long otherUserId = message.getSenderId().equals(userId) ? message.getReceiverId() : message.getSenderId();

                if (!conversationMap.containsKey(otherUserId)) {
                    User otherUser = userRepository.findById(otherUserId).orElse(null);

                    ConversationResponseDTO dto = new ConversationResponseDTO();
                    dto.setOtherUserId(otherUserId);
                    dto.setOtherUsername(otherUser != null ? otherUser.getUsername() : "Unknown");
                    dto.setLastMessage(message.getContent());
                    dto.setLastMessageTime(message.getCreatedAt());
                    dto.setProfilePicture( otherUser.getProfilePhotoUrl()  );

                    // Check for unread messages in this conversation
                    List<Message> conversationMessages = messageRepository.findConversationBetweenUsers(userId, otherUserId);
                    long unreadCount = conversationMessages.stream()
                            .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                            .count();

                    dto.setHasUnreadMessages(unreadCount > 0);
                    dto.setUnreadCount(unreadCount);

                    conversationMap.put(otherUserId, dto);
                    responseDTOs.add(dto);
                }
            }

            return ApiResponse.success(responseDTOs, "Conversations retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving conversations", "INTERNAL_SERVER_ERROR");
        }
    }

    // Get unread message count
    public ApiResponse<Long> getUnreadMessageCount(Long userId) {
        try {
            userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Long unreadCount = messageRepository.countUnreadMessagesForUser(userId);
            return ApiResponse.success(unreadCount, "Unread message count retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "USER_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving unread message count", "INTERNAL_SERVER_ERROR");
        }
    }

    // Mark messages as read
    private void markMessagesAsRead(Long receiverId, Long senderId) {
        List<Message> unreadMessages = messageRepository.findConversationBetweenUsers(receiverId, senderId)
                .stream()
                .filter(m -> m.getReceiverId().equals(receiverId) && !m.isRead())
                .collect(Collectors.toList());

        for (Message message : unreadMessages) {
            message.setRead(true);
            message.setReadAt(Instant.now()); // Changed from LocalDateTime.now() to Instant.now()
            messageRepository.save(message);
        }
    }
}