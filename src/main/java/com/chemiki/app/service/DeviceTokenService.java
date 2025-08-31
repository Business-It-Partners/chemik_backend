package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.RegisterDeviceTokenRequestDTO;
import com.chemiki.app.model.DeviceToken;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.DeviceTokenRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    private static final List<String> VALID_DEVICE_TYPES = Arrays.asList("ANDROID", "IOS", "WEB");

    /**
     * Register or update device token for a user
     */
    @Transactional
    public ApiResponse<String> registerDeviceToken(Long userId, RegisterDeviceTokenRequestDTO request) {
        try {
            // Validate user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found", "USER_NOT_FOUND");
            }

            // Validate device type
            if (!VALID_DEVICE_TYPES.contains(request.getDeviceType().toUpperCase())) {
                return ApiResponse.error("Invalid device type. Allowed: " + String.join(", ", VALID_DEVICE_TYPES), "INVALID_DEVICE_TYPE");
            }

            // Check if token already exists for this user
            Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserIdAndFcmToken(userId, request.getFcmToken());

            if (existingToken.isPresent()) {
                // Update existing token
                DeviceToken token = existingToken.get();
                token.setActive(true);
                token.setDeviceType(request.getDeviceType().toUpperCase());
                token.setDeviceInfo(request.getDeviceInfo());
                token.setUpdatedAt(LocalDateTime.now());
                token.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(token);

                log.info("Updated existing device token for user: {}", userId);
            } else {
                // Create new token
                DeviceToken newToken = new DeviceToken();
                newToken.setUserId(userId);
                newToken.setFcmToken(request.getFcmToken());
                newToken.setDeviceType(request.getDeviceType().toUpperCase());
                newToken.setDeviceInfo(request.getDeviceInfo());
                newToken.setActive(true);
                newToken.setCreatedAt(LocalDateTime.now());
                newToken.setUpdatedAt(LocalDateTime.now());
                newToken.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(newToken);

                // Optionally deactivate old tokens (keep only latest token per user)
//                 deviceTokenRepository.deactivateOldTokensForUser(userId, request.getFcmToken(), LocalDateTime.now());

                log.info("Registered new device token for user: {}", userId);
            }

            return ApiResponse.success("Token registered successfully", "Device token registered successfully");

        } catch (Exception e) {
            log.error("Error registering device token for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.error("Failed to register device token", "TOKEN_REGISTRATION_FAILED");
        }
    }

    /**
     * Get all active tokens for a user
     */
    public List<DeviceToken> getActiveTokensForUser(Long userId) {
        return deviceTokenRepository.findActiveTokensByUserId(userId);
    }

    /**
     * Get all active tokens except for specific user (for broadcast)
     */
    public List<DeviceToken> getActiveTokensExcludingUser(Long userId) {
        return deviceTokenRepository.findActiveTokensExcludingUser(userId);
    }

    /**
     * Get active tokens for regular users (non-institutional)
     */
    public List<DeviceToken> getActiveTokensForRegularUsers() {
        return deviceTokenRepository.findActiveTokensForRegularUsers();
    }

    /**
     * Mark token as used (update last used timestamp)
     */
    @Transactional
    public void markTokenAsUsed(String fcmToken) {
        try {
            deviceTokenRepository.updateLastUsedAt(fcmToken, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error updating last used time for token: {}", e.getMessage());
        }
    }

    /**
     * Mark token as inactive (when FCM returns error)
     */
    @Transactional
    public void markTokenAsInactive(String fcmToken) {
        try {
            Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByFcmTokenAndIsActiveTrue(fcmToken);
            if (tokenOpt.isPresent()) {
                DeviceToken token = tokenOpt.get();
                token.setActive(false);
                token.setUpdatedAt(LocalDateTime.now());
                deviceTokenRepository.save(token);
                log.info("Marked token as inactive: {}", fcmToken.substring(0, Math.min(fcmToken.length(), 20)) + "...");
            }
        } catch (Exception e) {
            log.error("Error marking token as inactive: {}", e.getMessage());
        }
    }

    /**
     * Cleanup inactive tokens older than 30 days (scheduled task)
     */
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    @Transactional
    public void cleanupInactiveTokens() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            deviceTokenRepository.removeInactiveTokensOlderThan(cutoffDate);
            log.info("Cleaned up inactive device tokens older than 30 days");
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage());
        }
    }

    /**
     * Get token count for user
     */
    public Long getActiveTokenCountForUser(Long userId) {
        return deviceTokenRepository.countActiveTokensByUserId(userId);
    }
}