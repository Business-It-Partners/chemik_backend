 package com.chemiki.app.controller;
import com.chemiki.app.dto.requestDto.NotificationRequestDTO;
import com.chemiki.app.dto.responseDto.NotificationResponseDTO;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chemiki.app.service.FCMService;
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final FCMService fcmService;

    public NotificationController(FCMService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDTO> sendNotification(@RequestBody NotificationRequestDTO request) {
        try {
            NotificationResponseDTO response = fcmService.sendNotification(request);
            return ResponseEntity.ok(response);
        } catch (FirebaseMessagingException e) {
            NotificationResponseDTO response = new NotificationResponseDTO(null, false, "Error sending notification: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}