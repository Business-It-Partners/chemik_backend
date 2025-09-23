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

import java.time.Instant;
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
     * Fixed to handle cross-user device scenarios
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
                return ApiResponse.error("Invalid device type. Allowed: " +
                        String.join(", ", VALID_DEVICE_TYPES), "INVALID_DEVICE_TYPE");
            }

            String fcmToken = request.getFcmToken();
            if (fcmToken == null || fcmToken.trim().isEmpty()) {
                return ApiResponse.error("FCM token cannot be empty", "INVALID_FCM_TOKEN");
            }

            // Check if this FCM token already exists for ANY user
            Optional<DeviceToken> existingTokenForAnyUser = deviceTokenRepository.findByFcmToken(fcmToken);

            if (existingTokenForAnyUser.isPresent()) {
                DeviceToken existingToken = existingTokenForAnyUser.get();

                if (existingToken.getUserId().equals(userId)) {
                    // Same user, same token - just update it
                    updateExistingToken(existingToken, request);
                    log.info("Updated existing device token for same user: {}", userId);
                } else {
                    // Different user has this token - transfer ownership
                    // This handles the Samsung->Oppo->Samsung scenario
                    log.info("Transferring FCM token from user {} to user {}",
                            existingToken.getUserId(), userId);

                    transferTokenToNewUser(existingToken, userId, request);
                }
            } else {
                // Completely new token - create new record
                createNewDeviceToken(userId, request);
                log.info("Created new device token for user: {}", userId);
            }

            return ApiResponse.success("Device token registered successfully",
                    "Device token registered successfully");

        } catch (Exception e) {
            log.error("Error registering device token for user {}: {}", userId, e.getMessage());
            return ApiResponse.error("Failed to register device token: " + e.getMessage(),
                    "REGISTRATION_ERROR");
        }
    }

    /**
     * Update existing token with new device info
     */
    private void updateExistingToken(DeviceToken token, RegisterDeviceTokenRequestDTO request) {
        token.setActive(true);
        token.setDeviceType(request.getDeviceType().toUpperCase());
        token.setDeviceInfo(request.getDeviceInfo());
        // @UpdateTimestamp will handle updatedAt automatically
        token.setLastUsedAt(Instant.now());
        deviceTokenRepository.save(token);
    }

    /**
     * Transfer token ownership to new user
     */
    private void transferTokenToNewUser(DeviceToken existingToken, Long newUserId,
                                        RegisterDeviceTokenRequestDTO request) {
        // Update the existing token to new user
        existingToken.setUserId(newUserId);
        existingToken.setActive(true);
        existingToken.setDeviceType(request.getDeviceType().toUpperCase());
        existingToken.setDeviceInfo(request.getDeviceInfo());
        // @UpdateTimestamp will handle updatedAt automatically
        existingToken.setLastUsedAt(Instant.now());
        deviceTokenRepository.save(existingToken);
    }

    /**
     * Create completely new device token
     */
    private void createNewDeviceToken(Long userId, RegisterDeviceTokenRequestDTO request) {
        DeviceToken newToken = new DeviceToken();
        newToken.setUserId(userId);
        newToken.setFcmToken(request.getFcmToken());
        newToken.setDeviceType(request.getDeviceType().toUpperCase());
        newToken.setDeviceInfo(request.getDeviceInfo());
        newToken.setActive(true);
        // @CreationTimestamp will handle createdAt automatically
        // @UpdateTimestamp will handle updatedAt automatically
        newToken.setLastUsedAt(Instant.now());
        deviceTokenRepository.save(newToken);
    }

    /**
     * Get active tokens for a user (for notifications)
     */
    public List<DeviceToken> getActiveTokensForUser(Long userId) {
        return deviceTokenRepository.findActiveTokensForUser(userId);
    }

    /**
     * Mark token as used (update last used timestamp)
     */
    @Transactional
    public void markTokenAsUsed(String fcmToken) {
        Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByFcmToken(fcmToken);
        if (tokenOpt.isPresent()) {
            DeviceToken token = tokenOpt.get();
            token.setLastUsedAt(Instant.now());
            deviceTokenRepository.save(token);
        }
    }

    /**
     * Mark token as inactive (when FCM returns error)
     */
    @Transactional
    public void markTokenAsInactive(String fcmToken) {
        Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByFcmToken(fcmToken);
        if (tokenOpt.isPresent()) {
            DeviceToken token = tokenOpt.get();
            token.setActive(false);
            // @UpdateTimestamp will handle updatedAt automatically
            deviceTokenRepository.save(token);
            log.info("Marked FCM token as inactive: {}...",
                    fcmToken.substring(0, Math.min(fcmToken.length(), 20)));
        }
    }

    /**
     * Clean up inactive tokens (scheduled task)
     * Run daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupInactiveTokens() {
        try {
            // Remove tokens inactive for more than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<DeviceToken> inactiveTokens = deviceTokenRepository
                    .findInactiveTokensOlderThan(cutoffDate);

            if (!inactiveTokens.isEmpty()) {
                deviceTokenRepository.deleteAll(inactiveTokens);
                log.info("Cleaned up {} inactive device tokens older than 30 days",
                        inactiveTokens.size());
            }
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage());
        }
    }
}