package com.must.courseevaluation.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 闲聊 Agent
 * 处理非课程推荐相关的对话
 */
@Component
public class ChatAgent implements BaseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatAgent.class);
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 预定义回复
    private static final Map<String, String> QUICK_RESPONSES = new HashMap<>();
    
    static {
        QUICK_RESPONSES.put("你好", "你好！👋 我是课程推荐助手，很高兴为您服务。请告诉我您想学习什么类型的课程，我来为您推荐！");
        QUICK_RESPONSES.put("hi", "Hi! 👋 我是课程推荐助手，请问有什么可以帮您的？");
        QUICK_RESPONSES.put("hello", "Hello! 👋 欢迎使用课程推荐系统，请告诉我您的学习需求。");
        QUICK_RESPONSES.put("谢谢", "不客气！😊 如果还有其他问题，随时可以问我。祝您学习愉快！");
        QUICK_RESPONSES.put("感谢", "很高兴能帮到您！😊 如果需要更多推荐，随时告诉我。");
        QUICK_RESPONSES.put("再见", "再见！👋 祝您学习进步，有需要随时回来找我！");
        QUICK_RESPONSES.put("拜拜", "拜拜！👋 期待下次为您服务！");
        QUICK_RESPONSES.put("好的", "好的！如果您有其他问题或想了解更多课程，随时告诉我。😊");
        QUICK_RESPONSES.put("可以", "好的，收到！有什么其他需要帮助的吗？");
        QUICK_RESPONSES.put("嗯", "好的，还有什么可以帮您的吗？比如推荐某个领域的课程？");
    }
    
    @Override
    public IntentType getSupportedIntentType() {
        return IntentType.CHAT;
    }
    
    @Override
    public AgentResult process(IntentResult intent, ConversationContext context) {
        logger.info("[ChatAgent] 处理闲聊: {}", intent.getOriginalMessage());
        
        String message = intent.getOriginalMessage().trim().toLowerCase();
        
        // 检查预定义回复
        for (Map.Entry<String, String> entry : QUICK_RESPONSES.entrySet()) {
            if (message.contains(entry.getKey())) {
                return AgentResult.text(IntentType.CHAT, entry.getValue());
            }
        }
        
        // 使用AI生成回复
        try {
            String response = generateAIChatResponse(intent.getOriginalMessage(), context);
            return AgentResult.text(IntentType.CHAT, response);
        } catch (Exception e) {
            logger.warn("AI闲聊回复生成失败: {}", e.getMessage());
            return AgentResult.text(IntentType.CHAT, 
                "我是课程推荐助手，主要帮您推荐合适的课程。请告诉我您想学习什么领域的知识，我来为您推荐！😊");
        }
    }
    
    private String generateAIChatResponse(String userMessage, ConversationContext context) throws Exception {
        String systemPrompt = """
            你是一个友好的课程推荐助手。用户可能会和你闲聊，你需要：
            1. 友好地回应用户
            2. 适当引导用户回到课程推荐话题
            3. 回复要简洁（50字以内）
            4. 可以适当使用emoji
            5. 保持专业但亲切的语气
            
            你的主要功能是帮助用户：
            - 推荐适合的课程
            - 比较不同课程
            - 介绍课程详情
            - 解答选课疑问
            """;
        
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("用户消息：").append(userMessage);
        
        if (context.hasLastRecommendedCourses()) {
            userPrompt.append("\n\n（之前推荐过一些课程，可以适当提及）");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.8);
        requestBody.put("max_tokens", 100);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt.toString()));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = apiUrl + "/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        }
        
        throw new RuntimeException("AI回复生成失败");
    }
}

