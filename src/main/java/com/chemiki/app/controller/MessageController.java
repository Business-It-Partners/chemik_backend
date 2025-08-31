package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.SendMessageRequestDTO;
import com.chemiki.app.dto.responseDto.ConversationResponseDTO;
import com.chemiki.app.dto.responseDto.MessageResponseDTO;
import com.chemiki.app.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Send a message
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(
            @Valid @RequestBody SendMessageRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long senderId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<MessageResponseDTO> response = messageService.sendMessage(request, senderId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "USER_NOT_FOUND":
                    status = HttpStatus.NOT_FOUND;
                    break;
                case "INVALID_RECEIVER":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get conversation with another user
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> getConversation(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<MessageResponseDTO>> response = messageService.getConversation(userId, otherUserId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("USER_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get all conversations for current user
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponseDTO>>> getUserConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<ConversationResponseDTO>> response = messageService.getUserConversations(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("USER_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // Get unread message count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Long> response = messageService.getUnreadMessageCount(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("USER_NOT_FOUND") ?
                    HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }
}