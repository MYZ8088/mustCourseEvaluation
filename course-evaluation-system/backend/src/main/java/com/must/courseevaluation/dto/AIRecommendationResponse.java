package com.must.courseevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI课程推荐响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {
    
    /**
     * 响应类型：'text' 或 'recommendation'
     */
    private String type;
    
    /**
     * 响应内容（文本消息或推荐说明）
     */
    private String content;
    
    /**
     * 推荐的课程列表（仅当type='recommendation'时有值）
     */
    private List<CourseRecommendation> courses = new ArrayList<>();
    
    /**
     * 更新后的上下文参数（用于多轮对话）
     */
    private Map<String, Object> updatedContext = new HashMap<>();
    
    /**
     * 是否成功
     */
    private boolean success = true;
    
    /**
     * 错误信息（失败时）
     */
    private String error;
    
    /**
     * 课程推荐项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRecommendation {
        private Long id;
        private String code;
        private String name;
        private Double credits;
        private String type;  // COMPULSORY 或 ELECTIVE
        private String description;
        private String facultyName;
        private String teacherName;
        private Double averageRating;
        private Integer reviewCount;
        private String reason;  // AI生成的推荐理由
    }
    
    /**
     * 创建文本响应
     */
    public static AIRecommendationResponse text(String content) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        response.setType("text");
        response.setContent(content);
        return response;
    }
    
    /**
     * 创建推荐响应
     */
    public static AIRecommendationResponse recommendation(String content, List<CourseRecommendation> courses) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        response.setType("recommendation");
        response.setContent(content);
        response.setCourses(courses);
        return response;
    }
    
    /**
     * 创建错误响应
     */
    public static AIRecommendationResponse error(String errorMessage) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        response.setType("text");
        response.setSuccess(false);
        response.setError(errorMessage);
        response.setContent("抱歉，处理您的请求时出现了问题：" + errorMessage);
        return response;
    }
}

