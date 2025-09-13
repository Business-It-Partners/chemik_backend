package com.chemiki.app.repository;

import com.chemiki.app.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    // For general users (phone-based reset)
    Optional<PasswordReset> findByTokenAndPhoneNumberAndExpiresAtAfter(
            String token, String phoneNumber, LocalDateTime dateTime);

    // For institutional users (email-based reset)
    Optional<PasswordReset> findByTokenAndEmailAndExpiresAtAfter(
            String token, String email, LocalDateTime dateTime);

    // Generic method to find by token (regardless of delivery method)
    Optional<PasswordReset> findByTokenAndExpiresAtAfter(String token, LocalDateTime dateTime);

    // Find by phone number (to check if reset already requested)
    Optional<PasswordReset> findByPhoneNumber(String phoneNumber);

    // Find by email (to check if reset already requested)
    Optional<PasswordReset> findByEmail(String email);

    // Clean up expired password resets
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}