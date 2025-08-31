package com.chemiki.app.repository;

import com.chemiki.app.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get conversation between two users
    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
            "AND m.deleted = false " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(Long userId1, Long userId2);

    // Get all conversations for a user (latest message from each conversation)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId OR m.receiverId = :userId) " +
            "AND m.deleted = false " +
            "AND m.createdAt = (SELECT MAX(m2.createdAt) FROM Message m2 WHERE " +
            "((m2.senderId = m.senderId AND m2.receiverId = m.receiverId) OR " +
            "(m2.senderId = m.receiverId AND m2.receiverId = m.senderId)) " +
            "AND m2.deleted = false) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findLatestConversationsForUser(Long userId);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false AND m.deleted = false")
    Long countUnreadMessagesForUser(Long userId);
}