// folder: com.chemiki.app.repository
// purpose: Provides data access methods for Otp entity
package com.chemiki.app.repository;

import com.chemiki.app.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    // For general users (phone-based OTP)
    Optional<Otp> findByTokenAndPhoneNumberAndExpiresAtAfter(String token, String phoneNumber, LocalDateTime currentTime);

    // For institutional users (email-based OTP)
    Optional<Otp> findByTokenAndEmailAndExpiresAtAfter(String token, String email, LocalDateTime currentTime);

    // Generic method to find by token (regardless of delivery method)
    Optional<Otp> findByTokenAndExpiresAtAfter(String token, LocalDateTime currentTime);

    // Clean up expired OTPs
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}




