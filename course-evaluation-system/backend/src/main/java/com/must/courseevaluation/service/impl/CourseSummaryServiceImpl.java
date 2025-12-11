package com.must.courseevaluation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.must.courseevaluation.dto.CourseSummaryDto;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import com.must.courseevaluation.service.CourseSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CourseSummaryServiceImpl implements CourseSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSummaryServiceImpl.class);

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.enabled}")
    private boolean enabled;

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CourseSummaryServiceImpl(CourseRepository courseRepository, 
                                   ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isAIServiceAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty() && !apiKey.equals("sk-your-api-key-here");
    }

    @Override
    public int getReviewCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        List<Review> reviews = reviewRepository.findByCourseAndStatus(course, Review.ReviewStatus.APPROVED);
        return reviews.size();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseSummaryDto getCourseSummary(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        
        // 检查评论数量
        List<Review> reviews = reviewRepository.findByCourseAndStatus(course, Review.ReviewStatus.APPROVED);
        if (reviews.size() < 10) {
            return null; // 评论不足10条，返回null
        }
        
        // 只返回缓存的AI总结，不触发生成
        if (course.getAiSummary() != null && !course.getAiSummary().isEmpty()) {
            try {
                CourseSummaryDto dto = objectMapper.readValue(course.getAiSummary(), CourseSummaryDto.class);
                dto.setUpdatedAt(course.getAiSummaryUpdatedAt() != null ? 
                        course.getAiSummaryUpdatedAt().toString() : null);
                // 添加评论数信息
                dto.setReviewCount(course.getAiSummaryReviewCount());
                return dto;
            } catch (Exception e) {
                logger.warn("解析缓存的AI总结失败: {}", e.getMessage());
                return null;
            }
        }
        
        // 没有缓存的AI总结，返回null（不自动生成，等待启动时自动生成）
        return null;
    }

    @Override
    @Transactional
    public CourseSummaryDto regenerateCourseSummary(Long courseId) {
        if (!isAIServiceAvailable()) {
            throw new RuntimeException("AI服务未配置或未启用");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        List<Review> reviews = reviewRepository.findByCourseAndStatus(course, Review.ReviewStatus.APPROVED);

        if (reviews.isEmpty()) {
            throw new RuntimeException("该课程暂无评价，无法生成总结");
        }

        if (reviews.size() < 10) {
            throw new RuntimeException("评价数量不足10条，无法生成AI总结");
        }

        return generateAndSaveSummary(course, reviews);
    }

    /**
     * 生成AI总结并保存到数据库
     */
    @Transactional
    public CourseSummaryDto generateAndSaveSummary(Course course, List<Review> reviews) {
        // 如果评价超过50条，随机抽取50条
        List<Review> selectedReviews = reviews;
        if (reviews.size() > 50) {
            selectedReviews = new ArrayList<>(reviews);
            Collections.shuffle(selectedReviews);
            selectedReviews = selectedReviews.subList(0, 50);
        }

        // 构建提示词
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(course, selectedReviews, reviews.size());

        // 调用DeepSeek API
        try {
            CourseSummaryDto dto = callDeepSeekAPI(systemPrompt, userPrompt);
            
            // 将总结保存到数据库
            String summaryJson = objectMapper.writeValueAsString(dto);
            course.setAiSummary(summaryJson);
            course.setAiSummaryUpdatedAt(LocalDateTime.now());
            // 保存生成时的评论数量，用于后续判断是否需要重新生成
            course.setAiSummaryReviewCount(reviews.size());
            courseRepository.save(course);
            
            dto.setUpdatedAt(course.getAiSummaryUpdatedAt().toString());
            dto.setReviewCount(reviews.size());
            logger.info("课程 {} 的AI总结已生成并保存，当前评论数: {}", course.getCode(), reviews.size());
            
            return dto;
        } catch (Exception e) {
            logger.error("调用DeepSeek API失败: {}", e.getMessage());
            throw new RuntimeException("AI服务暂时不可用，请稍后再试");
        }
    }

    private String buildSystemPrompt() {
        return """
            你是一个客观、公正的课程评价分析师。你的任务是根据提供的课程信息和大量学生评价，生成一份全面、客观的课程总结报告。
            
            要求：
            1. 总结必须客观中立，综合正面和负面反馈
            2. 分析维度包括：总体评价、课程难度/作业量、教师授课风格、优缺点分析
            3. 语言简洁明了，使用中文
            
            必须严格按照以下JSON格式输出：
            {
              "overall": "总体评价摘要（50-100字）",
              "difficulty": "课程难度与作业量分析",
              "teaching": "教师授课风格与质量分析",
              "pros": ["优点1", "优点2", "优点3"],
              "cons": ["缺点1", "缺点2"],
              "suggestion": "给未来选课学生的建议"
            }
            """;
    }

    private String buildUserPrompt(Course course, List<Review> reviews, int totalReviewCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("课程信息：\n");
        prompt.append("代码：").append(course.getCode()).append("\n");
        prompt.append("名称：").append(course.getName()).append("\n");
        if (course.getTeacher() != null) {
            prompt.append("教师：").append(course.getTeacher().getName()).append("\n");
        }
        prompt.append("简介：").append(course.getDescription() != null ? course.getDescription() : "无").append("\n\n");

        prompt.append("学生评价集合（共").append(totalReviewCount).append("条");
        if (totalReviewCount > 50) {
            prompt.append("，随机抽取50条");
        }
        prompt.append("）：\n");

        int index = 1;
        for (Review review : reviews) {
            prompt.append(index++).append(". 评分：").append(review.getRating()).append("分 | 内容：")
                  .append(review.getContent()).append("\n");
        }

        prompt.append("\n请根据以上信息生成课程总结。");
        return prompt.toString();
    }

    private CourseSummaryDto callDeepSeekAPI(String systemPrompt, String userPrompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 1000);
        requestBody.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = apiUrl + "/chat/completions";
        logger.info("调用DeepSeek API: {}", url);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            JsonNode summaryJson = objectMapper.readTree(content);
            
            CourseSummaryDto dto = new CourseSummaryDto();
            dto.setOverall(summaryJson.path("overall").asText());
            dto.setDifficulty(summaryJson.path("difficulty").asText());
            dto.setTeaching(summaryJson.path("teaching").asText());
            dto.setSuggestion(summaryJson.path("suggestion").asText());
            
            List<String> pros = new ArrayList<>();
            summaryJson.path("pros").forEach(node -> pros.add(node.asText()));
            dto.setPros(pros);
            
            List<String> cons = new ArrayList<>();
            summaryJson.path("cons").forEach(node -> cons.add(node.asText()));
            dto.setCons(cons);
            
            return dto;
        } else {
            throw new RuntimeException("DeepSeek API返回错误: " + response.getStatusCode());
        }
    }
}
