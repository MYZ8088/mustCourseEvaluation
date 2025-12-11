package com.must.courseevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIConversationDto {
    
    private Long id;
    private String conversationId;
    private Long userId;
    private String title;
    private Map<String, Object> context = new HashMap<>();
    private List<AIMessageDto> messages = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





















