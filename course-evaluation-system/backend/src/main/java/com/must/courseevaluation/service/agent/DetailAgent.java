package com.must.courseevaluation.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
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
 * 课程详情 Agent
 * 处理用户询问特定课程详细信息的请求
 */
@Component
public class DetailAgent implements BaseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(DetailAgent.class);
    
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
    
    public DetailAgent(CourseRepository courseRepository, ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
    }
    
    @Override
    public IntentType getSupportedIntentType() {
        return IntentType.DETAIL;
    }
    
    @Override
    public AgentResult process(IntentResult intent, ConversationContext context) {
        logger.info("[DetailAgent] 处理课程详情查询: {}", intent.getOriginalMessage());
        
        String courseName = intent.getCourseToQuery();
        
        // 如果没有指定课程名，尝试从上下文中获取
        if ((courseName == null || courseName.isEmpty()) && context.hasLastRecommendedCourses()) {
            // 可能用户说的是"这门课怎么样"，取第一门
            courseName = context.getLastRecommendedCourses().get(0).getName();
        }
        
        if (courseName == null || courseName.isEmpty()) {
            return AgentResult.text(IntentType.DETAIL, 
                "请告诉我您想了解哪门课程的详情。");
        }
        
        // 查找课程
        Optional<Course> courseOpt = findCourseByName(courseName);
        
        if (courseOpt.isEmpty()) {
            return AgentResult.text(IntentType.DETAIL, 
                "抱歉，我找不到\"" + courseName + "\"这门课程。请确认课程名称是否正确。");
        }
        
        Course course = courseOpt.get();
        
        // 生成详细介绍
        String detail = generateCourseDetail(course, intent.getOriginalMessage());
        
        // 转换为 CourseInfo
        AgentResult.CourseInfo courseInfo = toCourseInfo(course);
        
        return AgentResult.builder()
                .intentType(IntentType.DETAIL)
                .success(true)
                .courseDetail(courseInfo)
                .message(detail)
                .build();
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
    
    private String generateCourseDetail(Course course, String userMessage) {
        try {
            return generateAIDetail(course, userMessage);
        } catch (Exception e) {
            logger.warn("AI详情生成失败，使用默认详情: {}", e.getMessage());
            return generateDefaultDetail(course);
        }
    }
    
    private String generateAIDetail(Course course, String userMessage) throws Exception {
        // 获取课程评价
        List<Review> reviews = reviewRepository.findByCourse(course);
        
        String systemPrompt = """
            你是一个专业的课程顾问。请根据课程信息和学生评价，为用户详细介绍这门课程。
            
            要求：
            1. 全面介绍课程内容和特点
            2. 总结学生评价中的优点和注意事项
            3. 给出适合人群建议
            4. 语气友好自然
            5. 适当使用emoji
            6. 控制在250-350字
            """;
        
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("用户问题：").append(userMessage).append("\n\n");
        
        userPrompt.append("【课程信息】\n");
        userPrompt.append("- 名称：").append(course.getName()).append("\n");
        userPrompt.append("- 代码：").append(course.getCode()).append("\n");
        userPrompt.append("- 学院：").append(course.getFaculty() != null ? course.getFaculty().getName() : "未知").append("\n");
        userPrompt.append("- 学分：").append(course.getCredits()).append("\n");
        userPrompt.append("- 类型：").append(course.getType().name().equals("COMPULSORY") ? "必修课" : "选修课").append("\n");
        userPrompt.append("- 授课教师：").append(course.getTeacher() != null ? course.getTeacher().getName() : "未知").append("\n");
        
        Double rating = getAverageRating(course);
        Integer reviewCount = getReviewCount(course);
        userPrompt.append("- 评分：").append(rating != null ? String.format("%.1f", rating) : "暂无").append("\n");
        userPrompt.append("- 评价数：").append(reviewCount != null ? reviewCount : 0).append("\n");
        userPrompt.append("- 简介：").append(course.getDescription() != null ? course.getDescription() : "暂无").append("\n");
        
        if (!reviews.isEmpty()) {
            userPrompt.append("\n【学生评价摘要】\n");
            int count = 0;
            for (Review review : reviews) {
                if (count >= 3) break;
                userPrompt.append("- [评分").append(review.getRating()).append("] ")
                         .append(review.getContent()).append("\n");
                count++;
            }
        }
        
        userPrompt.append("\n请详细介绍这门课程。");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 600);

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
        
        throw new RuntimeException("AI详情生成失败");
    }
    
    private String generateDefaultDetail(Course course) {
        StringBuilder sb = new StringBuilder();
        sb.append("📚 **").append(course.getName()).append("**\n\n");
        
        sb.append("**基本信息**\n");
        sb.append("- 课程代码：").append(course.getCode()).append("\n");
        sb.append("- 所属学院：").append(course.getFaculty() != null ? course.getFaculty().getName() : "未知").append("\n");
        sb.append("- 学分：").append(course.getCredits()).append("\n");
        sb.append("- 类型：").append(course.getType().name().equals("COMPULSORY") ? "必修课" : "选修课").append("\n");
        sb.append("- 授课教师：").append(course.getTeacher() != null ? course.getTeacher().getName() : "未知").append("\n");
        
        Double rating = getAverageRating(course);
        sb.append("- 综合评分：").append(rating != null ? String.format("%.1f", rating) + " ⭐" : "暂无评分").append("\n");
        
        if (course.getDescription() != null && !course.getDescription().isEmpty()) {
            sb.append("\n**课程简介**\n");
            sb.append(course.getDescription()).append("\n");
        }
        
        sb.append("\n💡 点击课程卡片可以查看完整的课程详情和学生评价。");
        
        return sb.toString();
    }
    
    private AgentResult.CourseInfo toCourseInfo(Course course) {
        return AgentResult.CourseInfo.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .type(course.getType().name())
                .description(course.getDescription())
                .facultyName(course.getFaculty() != null ? course.getFaculty().getName() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .averageRating(getAverageRating(course))
                .reviewCount(getReviewCount(course))
                .build();
    }
    
    private Double getAverageRating(Course course) {
        return reviewRepository.getAverageRatingForCourse(course);
    }
    
    private Integer getReviewCount(Course course) {
        Long count = reviewRepository.getReviewCountForCourse(course);
        return count != null ? count.intValue() : 0;
    }
}

