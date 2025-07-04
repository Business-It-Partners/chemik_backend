package com.chemiki.app.repository;

import com.chemiki.app.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE j.deleted = false AND j.isOpen = true ORDER BY j.createdAt DESC")
    List<Job> findAllOpenOrderByCreatedAtDesc();

    Optional<Job> findByIdAndDeletedFalse(Long id);

    @Query("SELECT j FROM Job j WHERE j.deleted = false AND j.userId = :userId ORDER BY j.createdAt DESC")
    List<Job> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}