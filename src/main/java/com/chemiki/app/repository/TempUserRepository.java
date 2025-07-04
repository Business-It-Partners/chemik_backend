// folder: com.chemiki.app.repository
// purpose: Provides data access methods for TempUser entity
package com.chemiki.app.repository;

import com.chemiki.app.model.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser, Long> {
    Optional<TempUser> findByPhoneNumber(String phoneNumber);

}

