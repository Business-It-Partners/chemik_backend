package com.chemiki.app.repository;

import com.chemiki.app.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Add this
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Modifying // Add this
    void deleteByEmail(String email);

    Optional<Otp> findByTokenAndEmailAndExpiresAtAfter(String token, String email, Instant currentTime);

    void deleteByExpiresAtBefore(Instant currentTime); // Already provided, ensure it’s used
}