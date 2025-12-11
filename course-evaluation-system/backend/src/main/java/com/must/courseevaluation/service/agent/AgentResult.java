package com.must.courseevaluation.service.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent 处理结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {
    
    /**
     * 处理的意图类型
     */
    private IntentType intentType;
    
    /**
     * 是否成功
     */
    @Builder.Default
    private boolean success = true;
    
    /**
     * 推荐的课程列表（用于 NEW_QUERY, REFINE, SUPPLEMENT）
     */
    @Builder.Default
    private List<CourseInfo> courses = new ArrayList<>();
    
    /**
     * 文本消息（用于 CHAT, 错误信息等）
     */
    private String message;
    
    /**
     * 课程比较结果（用于 COMPARE）
     */
    private String comparisonResult;
    
    /**
     * 课程详情（用于 DETAIL）
     */
    private CourseInfo courseDetail;
    
    /**
     * 更新后的上下文参数
     */
    private Map<String, Object> updatedContext;
    
    /**
     * 课程信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfo {
        private Long id;
        private String code;
        private String name;
        private Double credits;
        private String type;
        private String description;
        private String facultyName;
        private String teacherName;
        private Double averageRating;
        private Integer reviewCount;
        private String reason;  // 推荐理由
    }
    
    /**
     * 创建成功的推荐结果
     */
    public static AgentResult recommendation(IntentType type, List<CourseInfo> courses, Map<String, Object> context) {
        return AgentResult.builder()
                .intentType(type)
                .success(true)
                .courses(courses)
                .updatedContext(context)
                .build();
    }
    
    /**
     * 创建文本消息结果
     */
    public static AgentResult text(IntentType type, String message) {
        return AgentResult.builder()
                .intentType(type)
                .success(true)
                .message(message)
                .build();
    }
    
    /**
     * 创建错误结果
     */
    public static AgentResult error(String message) {
        return AgentResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}

