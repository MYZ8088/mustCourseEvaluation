package com.must.courseevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI课程推荐聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
    
    /**
     * 对话上下文（包含之前提取的参数）
     */
    private Map<String, Object> context = new HashMap<>();
    
    /**
     * 对话ID（可选，用于关联对话历史）
     */
    private String conversationId;
    
    /**
     * 对话历史（用于上下文记忆）
     */
    private List<MessageHistory> conversationHistory = new ArrayList<>();
    
    /**
     * 消息历史记录项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageHistory {
        /**
         * 消息角色：user 或 ai
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
}
