package com.chemiki.app.repository;

import com.chemiki.app.model.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    // Check if user has already viewed this post
    @Query("SELECT COUNT(pv) > 0 FROM PostView pv WHERE pv.postId = :postId AND pv.userId = :userId")
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // Count total unique views for a post
    @Query("SELECT COUNT(pv) FROM PostView pv WHERE pv.postId = :postId")
    Long countViewsByPostId(Long postId);
}