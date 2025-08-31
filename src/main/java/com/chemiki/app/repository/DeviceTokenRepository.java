package com.chemiki.app.repository;

import com.chemiki.app.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    // Find all active tokens for a specific user
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId = :userId AND dt.isActive = true")
    List<DeviceToken> findActiveTokensByUserId(Long userId);

    // Find all active tokens except for specific user (for broadcast notifications)
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId != :excludeUserId AND dt.isActive = true")
    List<DeviceToken> findActiveTokensExcludingUser(Long excludeUserId);

    // Find by token value
    Optional<DeviceToken> findByFcmTokenAndIsActiveTrue(String fcmToken);

    // Find by user and token
    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);

    // Deactivate old tokens for a user when new token is registered (keep only the latest)
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.isActive = false, dt.updatedAt = :now WHERE dt.userId = :userId AND dt.fcmToken != :currentToken")
    void deactivateOldTokensForUser(Long userId, String currentToken, LocalDateTime now);

    // Update last used timestamp
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.lastUsedAt = :now WHERE dt.fcmToken = :token")
    void updateLastUsedAt(String token, LocalDateTime now);

    // Get all tokens for non-institutional users (regular users should receive institutional posts)
    @Query("SELECT dt FROM DeviceToken dt JOIN User u ON dt.userId = u.id WHERE u.isInstitutionalUser = false AND dt.isActive = true")
    List<DeviceToken> findActiveTokensForRegularUsers();

    // Count active tokens for a user
    @Query("SELECT COUNT(dt) FROM DeviceToken dt WHERE dt.userId = :userId AND dt.isActive = true")
    Long countActiveTokensByUserId(Long userId);

    // Remove inactive tokens older than specified days
    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken dt WHERE dt.isActive = false AND dt.updatedAt < :cutoffDate")
    void removeInactiveTokensOlderThan(LocalDateTime cutoffDate);
}