// Purpose: JPA repository interface for performing database operations on the RefreshToken entity.
package com.chemiki.app.repository;
import com.chemiki.app.model.RefreshToken;
 import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}