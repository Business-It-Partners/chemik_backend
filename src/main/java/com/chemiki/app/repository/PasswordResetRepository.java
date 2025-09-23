package com.chemiki.app.repository;

import com.chemiki.app.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

     Optional<PasswordReset> findByTokenAndExpiresAtAfter(String token, Instant dateTime);


    // Find by email (to check if reset already requested)
    Optional<PasswordReset> findByEmail(String email);

    // Clean up expired password resets
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}