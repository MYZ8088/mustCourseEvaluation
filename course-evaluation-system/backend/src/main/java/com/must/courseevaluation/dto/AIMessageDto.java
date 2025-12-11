package com.must.courseevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageDto {
    
    private Long id;
    private String messageId;
    private String conversationId;
    private String role;  // 'user' 或 'ai'
    private String content;
    private String messageType;  // 'text' 或 'recommendation'
    private List<Map<String, Object>> courses = new ArrayList<>();
    private LocalDateTime createdAt;
}




















