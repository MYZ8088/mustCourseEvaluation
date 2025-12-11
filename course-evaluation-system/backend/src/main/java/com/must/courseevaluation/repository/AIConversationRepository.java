package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.AIConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {
    
    /**
     * 根据conversation_id查找对话
     */
    Optional<AIConversation> findByConversationId(String conversationId);
    
    /**
     * 查找用户的所有对话（按创建时间倒序）
     */
    @Query("SELECT c FROM AIConversation c WHERE c.user.id = :userId AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<AIConversation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 查找用户最近的N个对话
     */
    @Query("SELECT c FROM AIConversation c WHERE c.user.id = :userId AND c.isDeleted = false ORDER BY c.updatedAt DESC")
    List<AIConversation> findRecentConversationsByUserId(@Param("userId") Long userId);
    
    /**
     * 删除对话（软删除）
     */
    @Query("UPDATE AIConversation c SET c.isDeleted = true WHERE c.conversationId = :conversationId")
    void softDeleteByConversationId(@Param("conversationId") String conversationId);
}





















