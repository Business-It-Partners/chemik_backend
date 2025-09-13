package com.chemiki.app.repository;

import com.chemiki.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber); // Added custom query
    Optional<User> findByEmail(String email); // Added custom query for email
    List<User> findByInstitutionalUserTrue();
}