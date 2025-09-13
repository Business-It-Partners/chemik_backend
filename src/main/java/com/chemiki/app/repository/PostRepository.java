package com.chemiki.app.repository;

import com.chemiki.app.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // General Tab: All posts except NEWS and NOTICE (reverse chronological)
    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.isActive = true " +
            "AND p.postType NOT IN ('NEWS', 'NOTICE') " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findGeneralPosts(Pageable pageable);

    // News & Notice Tab: Only NEWS and NOTICE from institutional users (reverse chronological)
    @Query("SELECT p FROM Post p JOIN User u ON p.userId = u.id WHERE " +
            "p.deleted = false AND p.isActive = true " +
            "AND p.postType IN ('NEWS', 'NOTICE') " +
            "AND u.institutionalUser = true " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findNewsAndNoticePosts(Pageable pageable);

    // Get single post by ID (not deleted)
    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.deleted = false")
    Post findActivePostById(Long postId);

    // Get user's own posts
    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.deleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findUserPosts(Long userId, Pageable pageable);

    // Increment view count
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(Long postId);

    // Increment comment count
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(Long postId);

    // Decrement comment count (when comment is deleted)
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(Long postId);
}