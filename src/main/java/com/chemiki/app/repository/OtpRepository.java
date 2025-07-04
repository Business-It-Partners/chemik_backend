// folder: com.chemiki.app.repository
// purpose: Provides data access methods for Otp entity
package com.chemiki.app.repository;

import com.chemiki.app.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByTokenAndPhoneNumberAndExpiresAtAfter(String token, String phoneNumber, LocalDateTime dateTime);

}




