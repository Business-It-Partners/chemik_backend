package com.chemiki.app.repository;

import com.chemiki.app.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    // Find valid reset token (same pattern as OTP)
    Optional<PasswordReset> findByTokenAndPhoneNumberAndExpiresAtAfter(
            String token, String phoneNumber, LocalDateTime dateTime);

    // Find by phone number (to check if reset already requested)
    Optional<PasswordReset> findByPhoneNumber(String phoneNumber);
}