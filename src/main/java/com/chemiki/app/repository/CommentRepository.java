package com.chemiki.app.repository;

import com.chemiki.app.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Get all comments for a post (chronological order - oldest first, like traditional comments)
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.deleted = false " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByPostId(Long postId);

    // Get active comment by ID (not deleted)
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.deleted = false")
    Optional<Comment> findActiveCommentById(Long commentId);

    // Get user's comments
    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.deleted = false " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByUserId(Long userId);

    // Count active comments for a post
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.deleted = false")
    Long countActiveCommentsByPostId(Long postId);
}