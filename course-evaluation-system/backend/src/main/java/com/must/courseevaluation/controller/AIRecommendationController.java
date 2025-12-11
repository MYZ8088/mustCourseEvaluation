package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.AIConversationDto;
import com.must.courseevaluation.dto.AIMessageDto;
import com.must.courseevaluation.dto.AIRecommendationResponse;
import com.must.courseevaluation.dto.ChatRequest;
import com.must.courseevaluation.security.UserSecurity;
import com.must.courseevaluation.service.AIRecommendationService;
import com.must.courseevaluation.service.AICourseRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-recommendations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIRecommendationController {
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private AICourseRecommendationService aiCourseRecommendationService;
    
    @Autowired
    private UserSecurity userSecurity;
    
    /**
     * 获取当前用户的对话列表
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AIConversationDto>> getConversations() {
        Long userId = userSecurity.getCurrentUserId();
        List<AIConversationDto> conversations = aiRecommendationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    /**
     * 创建新对话
     */
    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIConversationDto> createConversation(@RequestBody Map<String, String> request) {
        Long userId = userSecurity.getCurrentUserId();
        String conversationId = request.get("conversationId");
        
        if (conversationId == null || conversationId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        AIConversationDto conversation = aiRecommendationService.createConversation(userId, conversationId);
        return new ResponseEntity<>(conversation, HttpStatus.CREATED);
    }
    
    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIConversationDto> getConversation(@PathVariable String conversationId) {
        Long userId = userSecurity.getCurrentUserId();
        AIConversationDto conversation = aiRecommendationService.getConversation(conversationId, userId);
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * 保存消息
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIMessageDto> saveMessage(
            @PathVariable String conversationId,
            @RequestBody AIMessageDto messageDto) {
        Long userId = userSecurity.getCurrentUserId();
        messageDto.setConversationId(conversationId);
        AIMessageDto saved = aiRecommendationService.saveMessage(conversationId, messageDto, userId);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    /**
     * 更新对话上下文
     */
    @PutMapping("/conversations/{conversationId}/context")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateContext(
            @PathVariable String conversationId,
            @RequestBody Map<String, Object> context) {
        Long userId = userSecurity.getCurrentUserId();
        aiRecommendationService.updateContext(conversationId, context, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新对话标题
     */
    @PutMapping("/conversations/{conversationId}/title")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateTitle(
            @PathVariable String conversationId,
            @RequestBody Map<String, String> request) {
        Long userId = userSecurity.getCurrentUserId();
        String title = request.get("title");
        aiRecommendationService.updateTitle(conversationId, title, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        Long userId = userSecurity.getCurrentUserId();
        aiRecommendationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 清空所有对话
     */
    @DeleteMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearAllConversations() {
        Long userId = userSecurity.getCurrentUserId();
        aiRecommendationService.clearAllConversations(userId);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== AI课程推荐聊天接口 ====================
    
    /**
     * AI课程推荐聊天接口
     * 接收用户消息，返回AI推荐结果
     */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIRecommendationResponse> chat(@RequestBody ChatRequest request) {
        try {
            AIRecommendationResponse response = aiCourseRecommendationService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(AIRecommendationResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查AI服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        boolean available = aiCourseRecommendationService.isAIServiceAvailable();
        return ResponseEntity.ok(Map.of(
            "available", available,
            "message", available ? "AI服务正常" : "AI服务未配置或未启用"
        ));
    }
}










