package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.AIConversationDto;
import com.must.courseevaluation.dto.AIMessageDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.AIConversation;
import com.must.courseevaluation.model.AIMessage;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.AIConversationRepository;
import com.must.courseevaluation.repository.AIMessageRepository;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.AIRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIRecommendationServiceImpl implements AIRecommendationService {
    
    @Autowired
    private AIConversationRepository conversationRepository;
    
    @Autowired
    private AIMessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<AIConversationDto> getUserConversations(Long userId) {
        List<AIConversation> conversations = conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return conversations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public AIConversationDto createConversation(Long userId, String conversationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 检查conversation_id是否已存在
        if (conversationRepository.findByConversationId(conversationId).isPresent()) {
            throw new IllegalArgumentException("对话ID已存在: " + conversationId);
        }
        
        AIConversation conversation = new AIConversation();
        conversation.setConversationId(conversationId);
        conversation.setUser(user);
        conversation.setTitle("新对话");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        AIConversation saved = conversationRepository.save(conversation);
        return convertToDto(saved);
    }
    
    @Override
    public AIConversationDto getConversation(String conversationId, Long userId) {
        AIConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在: " + conversationId));
        
        // 验证权限
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权访问该对话");
        }
        
        // 日志：查看从数据库加载的上下文
        Map<String, Object> ctx = conversation.getContext();
        logger.info("[加载上下文] conversationId={}, context keys={}", conversationId, ctx != null ? ctx.keySet() : "null");
        if (ctx != null) {
            logger.info("[加载上下文] dayOfWeek={}, faculty={}, timePeriod={}", 
                    ctx.get("dayOfWeek"), ctx.get("faculty"), ctx.get("timePeriod"));
        }
        
        return convertToDtoWithMessages(conversation);
    }
    
    @Override
    @Transactional
    public AIMessageDto saveMessage(String conversationId, AIMessageDto messageDto, Long userId) {
        AIConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在: " + conversationId));
        
        // 验证权限
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该对话");
        }
        
        AIMessage message = new AIMessage();
        message.setMessageId(messageDto.getMessageId());
        message.setConversation(conversation);
        message.setRole(messageDto.getRole());
        message.setContent(messageDto.getContent());
        message.setMessageType(messageDto.getMessageType());
        message.setCourses(messageDto.getCourses());
        message.setCreatedAt(LocalDateTime.now());
        
        AIMessage saved = messageRepository.save(message);
        
        // 更新对话的更新时间
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return convertMessageToDto(saved);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationServiceImpl.class);
    
    @Override
    @Transactional
    public void updateContext(String conversationId, Map<String, Object> context, Long userId) {
        AIConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在: " + conversationId));
        
        // 验证权限
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该对话");
        }
        
        logger.info("[保存上下文] conversationId={}, context keys={}", conversationId, context != null ? context.keySet() : "null");
        if (context != null) {
            logger.info("[保存上下文] dayOfWeek={}, faculty={}, timePeriod={}", 
                    context.get("dayOfWeek"), context.get("faculty"), context.get("timePeriod"));
        }
        
        conversation.setContext(context);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
    
    @Override
    @Transactional
    public void updateTitle(String conversationId, String title, Long userId) {
        AIConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在: " + conversationId));
        
        // 验证权限
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该对话");
        }
        
        conversation.setTitle(title);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
    
    @Override
    @Transactional
    public void deleteConversation(String conversationId, Long userId) {
        AIConversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("对话不存在: " + conversationId));
        
        // 验证权限
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该对话");
        }
        
        conversation.setIsDeleted(true);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
    
    @Override
    @Transactional
    public void clearAllConversations(Long userId) {
        List<AIConversation> conversations = conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (AIConversation conversation : conversations) {
            conversation.setIsDeleted(true);
            conversation.setUpdatedAt(LocalDateTime.now());
        }
        conversationRepository.saveAll(conversations);
    }
    
    // ========== 辅助方法 ==========
    
    private AIConversationDto convertToDto(AIConversation conversation) {
        AIConversationDto dto = new AIConversationDto();
        dto.setId(conversation.getId());
        dto.setConversationId(conversation.getConversationId());
        dto.setUserId(conversation.getUser().getId());
        dto.setTitle(conversation.getTitle());
        dto.setContext(conversation.getContext());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }
    
    private AIConversationDto convertToDtoWithMessages(AIConversation conversation) {
        AIConversationDto dto = convertToDto(conversation);
        
        List<AIMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        dto.setMessages(messages.stream()
                .map(this::convertMessageToDto)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    private AIMessageDto convertMessageToDto(AIMessage message) {
        AIMessageDto dto = new AIMessageDto();
        dto.setId(message.getId());
        dto.setMessageId(message.getMessageId());
        dto.setConversationId(message.getConversation().getConversationId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setCourses(message.getCourses());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}











