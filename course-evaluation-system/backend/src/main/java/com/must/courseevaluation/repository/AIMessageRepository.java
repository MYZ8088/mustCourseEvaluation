package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIMessageRepository extends JpaRepository<AIMessage, Long> {
    
    /**
     * 根据message_id查找消息
     */
    Optional<AIMessage> findByMessageId(String messageId);
    
    /**
     * 查找对话的所有消息（按创建时间升序）
     */
    @Query("SELECT m FROM AIMessage m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<AIMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
    
    /**
     * 根据conversation_id查找消息
     */
    @Query("SELECT m FROM AIMessage m WHERE m.conversation.conversationId = :conversationId ORDER BY m.createdAt ASC")
    List<AIMessage> findByConversationIdStringOrderByCreatedAtAsc(@Param("conversationId") String conversationId);
}





















