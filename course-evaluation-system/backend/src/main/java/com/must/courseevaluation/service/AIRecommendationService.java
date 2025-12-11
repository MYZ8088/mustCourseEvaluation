package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.AIConversationDto;
import com.must.courseevaluation.dto.AIMessageDto;

import java.util.List;
import java.util.Map;

public interface AIRecommendationService {
    
    /**
     * 获取用户的对话历史
     */
    List<AIConversationDto> getUserConversations(Long userId);
    
    /**
     * 创建新对话
     */
    AIConversationDto createConversation(Long userId, String conversationId);
    
    /**
     * 获取对话详情
     */
    AIConversationDto getConversation(String conversationId, Long userId);
    
    /**
     * 保存消息
     */
    AIMessageDto saveMessage(String conversationId, AIMessageDto messageDto, Long userId);
    
    /**
     * 更新对话上下文
     */
    void updateContext(String conversationId, Map<String, Object> context, Long userId);
    
    /**
     * 更新对话标题
     */
    void updateTitle(String conversationId, String title, Long userId);
    
    /**
     * 删除对话
     */
    void deleteConversation(String conversationId, Long userId);
    
    /**
     * 清空用户的所有对话
     */
    void clearAllConversations(Long userId);
}




















