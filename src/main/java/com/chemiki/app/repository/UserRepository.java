// folder: com.chemiki.app.repository
// purpose: Provides data access methods for User entity
package com.chemiki.app.repository;

import com.chemiki.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber); // Added custom query
}



