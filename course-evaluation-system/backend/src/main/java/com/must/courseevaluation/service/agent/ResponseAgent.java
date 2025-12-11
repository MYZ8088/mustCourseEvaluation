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
 * 响应生成 Agent
 * 将各Agent的处理结果包装成自然语言回复
 */
@Component
public class ResponseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseAgent.class);
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 为推荐结果生成话术
     */
    public String generateRecommendationResponse(AgentResult result, IntentResult intent, ConversationContext context) {
        if (result.getMessage() != null && !result.getMessage().isEmpty()) {
            // 已有消息（如错误信息）
            return result.getMessage();
        }
        
        if (result.getCourses() == null || result.getCourses().isEmpty()) {
            return "抱歉，没有找到符合条件的课程。";
        }
        
        try {
            return generateAIResponse(result, intent, context);
        } catch (Exception e) {
            logger.warn("AI话术生成失败，使用默认话术: {}", e.getMessage());
            return generateDefaultResponse(result, intent, context);
        }
    }
    
    private String generateAIResponse(AgentResult result, IntentResult intent, ConversationContext context) throws Exception {
        String systemPrompt = buildSystemPrompt(result.getIntentType());
        String userPrompt = buildUserPrompt(result, intent, context);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 300);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = apiUrl + "/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        }
        
        throw new RuntimeException("AI话术生成失败");
    }
    
    private String buildSystemPrompt(IntentType intentType) {
        String basePrompt = """
            你是一个友好、专业的课程推荐助手。请根据推荐结果，生成一段温暖、有帮助的介绍。
            
            要求：
            1. 开头要体现你理解了用户的需求
            2. 语气友好自然，像朋友推荐一样
            3. 控制在80-120字
            4. 适当使用emoji增加亲和力
            5. 直接输出文字，不要输出JSON或代码
            """;
        
        switch (intentType) {
            case REFINE:
                return basePrompt + "\n6. 强调这是从之前推荐的课程中筛选出来的";
            case SUPPLEMENT:
                return basePrompt + "\n6. 体现你根据用户新增的条件更新了推荐";
            default:
                return basePrompt;
        }
    }
    
    private String buildUserPrompt(AgentResult result, IntentResult intent, ConversationContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("用户原始问题：").append(intent.getOriginalMessage()).append("\n\n");
        
        prompt.append("意图类型：").append(result.getIntentType().name()).append("\n");
        
        if (context.getParameters() != null && !context.getParameters().isEmpty()) {
            prompt.append("\n用户需求：\n");
            Map<String, Object> params = context.getParameters();
            if (params.containsKey("faculty")) {
                prompt.append("- 学院：").append(params.get("faculty")).append("\n");
            }
            if (params.containsKey("courseType")) {
                String type = (String) params.get("courseType");
                prompt.append("- 类型：").append("COMPULSORY".equals(type) ? "必修课" : "选修课").append("\n");
            }
            if (params.containsKey("credits")) {
                prompt.append("- 学分：").append(params.get("credits")).append("\n");
            }
        }
        
        prompt.append("\n推荐的课程（共").append(result.getCourses().size()).append("门）：\n");
        for (AgentResult.CourseInfo course : result.getCourses()) {
            prompt.append("- ").append(course.getName());
            if (course.getFacultyName() != null) {
                prompt.append("（").append(course.getFacultyName()).append("）");
            }
            prompt.append("\n");
        }
        
        if (result.getIntentType() == IntentType.REFINE && context.hasLastRecommendedCourses()) {
            prompt.append("\n（这是从之前推荐的 ").append(context.getLastRecommendedCourses().size())
                  .append(" 门课程中筛选出来的）\n");
        }
        
        prompt.append("\n请生成推荐介绍。");
        
        return prompt.toString();
    }
    
    private String generateDefaultResponse(AgentResult result, IntentResult intent, ConversationContext context) {
        StringBuilder response = new StringBuilder();
        
        switch (result.getIntentType()) {
            case REFINE:
                response.append("📋 根据您的筛选条件，我从之前推荐的课程中找到了 ")
                       .append(result.getCourses().size()).append(" 门符合要求的课程：\n\n");
                break;
            case SUPPLEMENT:
                response.append("🔄 根据您更新的条件，我重新为您推荐了 ")
                       .append(result.getCourses().size()).append(" 门课程：\n\n");
                break;
            default:
                response.append("🎓 根据您的需求，我为您推荐了 ")
                       .append(result.getCourses().size()).append(" 门课程：\n\n");
        }
        
        // 添加学院信息
        Map<String, Object> params = context.getParameters();
        if (params != null && params.containsKey("faculty")) {
            response.append("🏫 学院：").append(params.get("faculty")).append("\n");
        }
        
        // 添加课程类型信息
        if (params != null && params.containsKey("courseType")) {
            String type = (String) params.get("courseType");
            response.append("📚 类型：").append("COMPULSORY".equals(type) ? "必修课" : "选修课").append("\n");
        }
        
        response.append("\n💡 点击课程卡片可以查看详细信息和学生评价！");
        
        return response.toString();
    }
}

