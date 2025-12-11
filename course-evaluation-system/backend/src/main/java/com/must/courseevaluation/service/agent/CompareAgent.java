package com.must.courseevaluation.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 课程比较 Agent
 * 处理用户比较多门课程的请求
 */
@Component
public class CompareAgent implements BaseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(CompareAgent.class);
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CompareAgent(CourseRepository courseRepository, ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
    }
    
    @Override
    public IntentType getSupportedIntentType() {
        return IntentType.COMPARE;
    }
    
    @Override
    public AgentResult process(IntentResult intent, ConversationContext context) {
        logger.info("[CompareAgent] 处理课程比较: {}", intent.getOriginalMessage());
        
        List<String> coursesToCompare = intent.getCoursesToCompare();
        
        // 如果没有指定课程名，尝试从上下文中获取
        if ((coursesToCompare == null || coursesToCompare.isEmpty()) && context.hasLastRecommendedCourses()) {
            // 可能用户说的是"比较这两门课"，取前两门
            if (context.getLastRecommendedCourses().size() >= 2) {
                coursesToCompare = Arrays.asList(
                    context.getLastRecommendedCourses().get(0).getName(),
                    context.getLastRecommendedCourses().get(1).getName()
                );
            }
        }
        
        if (coursesToCompare == null || coursesToCompare.size() < 2) {
            return AgentResult.text(IntentType.COMPARE, 
                "请告诉我您想比较哪些课程。例如：\"比较人工智能导论和数据库系统\"");
        }
        
        // 查找课程
        List<Course> courses = new ArrayList<>();
        for (String name : coursesToCompare) {
            Optional<Course> course = findCourseByName(name);
            course.ifPresent(courses::add);
        }
        
        if (courses.size() < 2) {
            return AgentResult.text(IntentType.COMPARE, 
                "抱歉，我找不到您提到的某些课程。请确认课程名称是否正确。");
        }
        
        // 生成比较结果
        String comparison = generateComparison(courses, intent.getOriginalMessage());
        
        AgentResult result = AgentResult.builder()
                .intentType(IntentType.COMPARE)
                .success(true)
                .comparisonResult(comparison)
                .message(comparison)
                .build();
        
        return result;
    }
    
    private Optional<Course> findCourseByName(String name) {
        List<Course> allCourses = courseRepository.findAll();
        
        // 精确匹配
        for (Course course : allCourses) {
            if (course.getName().equals(name)) {
                return Optional.of(course);
            }
        }
        
        // 模糊匹配
        for (Course course : allCourses) {
            if (course.getName().contains(name) || name.contains(course.getName())) {
                return Optional.of(course);
            }
        }
        
        return Optional.empty();
    }
    
    private String generateComparison(List<Course> courses, String userMessage) {
        try {
            return generateAIComparison(courses, userMessage);
        } catch (Exception e) {
            logger.warn("AI比较生成失败，使用默认比较: {}", e.getMessage());
            return generateDefaultComparison(courses);
        }
    }
    
    private String generateAIComparison(List<Course> courses, String userMessage) throws Exception {
        String systemPrompt = """
            你是一个专业的课程顾问。请根据提供的课程信息，帮助用户进行课程比较和选择建议。
            
            要求：
            1. 从多个维度比较课程：学院、学分、难度、评价、适合人群等
            2. 客观陈述各课程的优势和特点
            3. 最后给出个性化建议
            4. 语气友好自然
            5. 适当使用emoji
            6. 控制在200-300字
            """;
        
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("用户问题：").append(userMessage).append("\n\n");
        userPrompt.append("需要比较的课程：\n");
        
        for (Course course : courses) {
            userPrompt.append("\n【").append(course.getName()).append("】\n");
            userPrompt.append("- 学院：").append(course.getFaculty() != null ? course.getFaculty().getName() : "未知").append("\n");
            userPrompt.append("- 学分：").append(course.getCredits()).append("\n");
            userPrompt.append("- 类型：").append(course.getType().name().equals("COMPULSORY") ? "必修课" : "选修课").append("\n");
            userPrompt.append("- 授课教师：").append(course.getTeacher() != null ? course.getTeacher().getName() : "未知").append("\n");
            
            Double rating = getAverageRating(course);
            Integer reviewCount = getReviewCount(course);
            userPrompt.append("- 评分：").append(rating != null ? String.format("%.1f", rating) : "暂无").append("\n");
            userPrompt.append("- 评价数：").append(reviewCount != null ? reviewCount : 0).append("\n");
            userPrompt.append("- 简介：").append(course.getDescription() != null ? course.getDescription() : "暂无").append("\n");
        }
        
        userPrompt.append("\n请进行比较并给出建议。");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

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
        
        throw new RuntimeException("AI比较生成失败");
    }
    
    private String generateDefaultComparison(List<Course> courses) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 **课程对比分析**\n\n");
        
        for (Course course : courses) {
            sb.append("**").append(course.getName()).append("**\n");
            sb.append("- 学院：").append(course.getFaculty() != null ? course.getFaculty().getName() : "未知").append("\n");
            sb.append("- 学分：").append(course.getCredits()).append("\n");
            sb.append("- 类型：").append(course.getType().name().equals("COMPULSORY") ? "必修课" : "选修课").append("\n");
            
            Double rating = getAverageRating(course);
            sb.append("- 评分：").append(rating != null ? String.format("%.1f", rating) : "暂无").append("\n\n");
        }
        
        sb.append("💡 建议您根据自己的学习目标和时间安排来选择适合的课程。");
        
        return sb.toString();
    }
    
    private Double getAverageRating(Course course) {
        return reviewRepository.getAverageRatingForCourse(course);
    }
    
    private Integer getReviewCount(Course course) {
        Long count = reviewRepository.getReviewCountForCourse(course);
        return count != null ? count.intValue() : 0;
    }
}

