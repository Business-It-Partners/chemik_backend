package com.chemiki.app.repository;

import com.chemiki.app.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * Find device token by FCM token (regardless of user)
     * 🔥 KEY METHOD: This enables cross-user token management
     */
    Optional<DeviceToken> findByFcmToken(String fcmToken);

    /**
     * Find device token by user ID and FCM token
     */
    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);

    /**
     * Get all device tokens for a user
     */
    List<DeviceToken> findByUserId(Long userId);

    /**
     * Get only active tokens for a user (for sending notifications)
     */
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId = :userId AND dt.isActive = true")
    List<DeviceToken> findActiveTokensForUser(@Param("userId") Long userId);

    /**
     * Find inactive tokens older than specified date (for cleanup)
     */
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.isActive = false AND dt.updatedAt < :cutoffDate")
    List<DeviceToken> findInactiveTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(dt) FROM DeviceToken dt WHERE dt.userId = :userId AND dt.isActive = true")
    long countActiveTokensForUser(@Param("userId") Long userId);

    /**
     * Get all tokens for a specific device type
     */
    List<DeviceToken> findByDeviceTypeAndIsActive(String deviceType, boolean isActive);

    /**
     * Find tokens that haven't been used for a long time (for cleanup)
     */
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.lastUsedAt < :cutoffDate")
    List<DeviceToken> findTokensNotUsedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if a specific FCM token exists and is active
     */
    @Query("SELECT CASE WHEN COUNT(dt) > 0 THEN true ELSE false END FROM DeviceToken dt WHERE dt.fcmToken = :fcmToken AND dt.isActive = true")
    boolean existsByFcmTokenAndIsActive(@Param("fcmToken") String fcmToken);
}