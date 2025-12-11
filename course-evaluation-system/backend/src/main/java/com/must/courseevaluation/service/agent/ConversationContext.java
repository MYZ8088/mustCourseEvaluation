package com.must.courseevaluation.service.agent;

import com.must.courseevaluation.dto.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationContext.class);
    
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * 用户ID（可选，用于获取用户课程表）
     */
    private Long userId;
    
    /**
     * 上次推荐的课程列表
     */
    @Builder.Default
    private List<AgentResult.CourseInfo> lastRecommendedCourses = new ArrayList<>();
    
    /**
     * 对话历史
     */
    @Builder.Default
    private List<ChatRequest.MessageHistory> history = new ArrayList<>();
    
    /**
     * 搜索参数（学院、课程类型、学分等）
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * 从请求的 context Map 构建 ConversationContext（无userId）
     */
    public static ConversationContext fromMap(Map<String, Object> contextMap, 
            List<ChatRequest.MessageHistory> history, String conversationId) {
        return fromMap(contextMap, history, conversationId, null);
    }
    
    /**
     * 从请求的 context Map 构建 ConversationContext
     */
    public static ConversationContext fromMap(Map<String, Object> contextMap, 
            List<ChatRequest.MessageHistory> history, String conversationId, Long userId) {
        ConversationContext context = new ConversationContext();
        context.setConversationId(conversationId);
        context.setUserId(userId);
        context.setHistory(history != null ? history : new ArrayList<>());
        context.setParameters(new HashMap<>());
        
        if (contextMap != null) {
            // 提取上次推荐的课程
            if (contextMap.containsKey("lastRecommendedCourses")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> courses = (List<Map<String, Object>>) contextMap.get("lastRecommendedCourses");
                if (courses != null) {
                    List<AgentResult.CourseInfo> courseInfos = new ArrayList<>();
                    for (Map<String, Object> course : courses) {
                        AgentResult.CourseInfo info = AgentResult.CourseInfo.builder()
                                .id(course.get("id") != null ? ((Number) course.get("id")).longValue() : null)
                                .code((String) course.get("code"))
                                .name((String) course.get("name"))
                                .credits(course.get("credits") != null ? ((Number) course.get("credits")).doubleValue() : null)
                                .type((String) course.get("type"))
                                .description((String) course.get("description"))
                                .facultyName((String) course.get("facultyName"))
                                .teacherName((String) course.get("teacherName"))
                                .averageRating(course.get("averageRating") != null ? ((Number) course.get("averageRating")).doubleValue() : null)
                                .reviewCount(course.get("reviewCount") != null ? ((Number) course.get("reviewCount")).intValue() : null)
                                .build();
                        courseInfos.add(info);
                    }
                    context.setLastRecommendedCourses(courseInfos);
                }
            }
            
            // 提取搜索参数
            Map<String, Object> params = context.getParameters();
            logger.info("[ConversationContext] 开始从contextMap提取参数，contextMap keys: {}", contextMap.keySet());
            
            if (contextMap.containsKey("faculty")) {
                params.put("faculty", contextMap.get("faculty"));
                logger.info("[ConversationContext] 提取faculty: {}", contextMap.get("faculty"));
            }
            if (contextMap.containsKey("teacher")) {
                params.put("teacher", contextMap.get("teacher"));
            }
            if (contextMap.containsKey("courseType")) {
                params.put("courseType", contextMap.get("courseType"));
            }
            if (contextMap.containsKey("credits")) {
                params.put("credits", contextMap.get("credits"));
            }
            if (contextMap.containsKey("difficulty")) {
                params.put("difficulty", contextMap.get("difficulty"));
            }
            // 提取时间参数 - dayOfWeek 支持 List 或单个数值
            if (contextMap.containsKey("dayOfWeek")) {
                Object dayOfWeek = contextMap.get("dayOfWeek");
                logger.info("[ConversationContext] 发现dayOfWeek: {} (类型: {})", dayOfWeek, 
                        dayOfWeek != null ? dayOfWeek.getClass().getName() : "null");
                if (dayOfWeek instanceof List) {
                    // List 形式 - 直接使用
                    @SuppressWarnings("unchecked")
                    List<Object> days = (List<Object>) dayOfWeek;
                    List<Integer> dayList = new ArrayList<>();
                    for (Object d : days) {
                        if (d instanceof Number) {
                            dayList.add(((Number) d).intValue());
                        }
                    }
                    if (!dayList.isEmpty()) {
                        params.put("dayOfWeek", dayList);
                        logger.info("[ConversationContext] 成功提取dayOfWeek(List): {}", dayList);
                    }
                } else if (dayOfWeek instanceof Number) {
                    // 单值形式 - 转为 List 统一处理
                    int day = ((Number) dayOfWeek).intValue();
                    params.put("dayOfWeek", new ArrayList<>(List.of(day)));
                    logger.info("[ConversationContext] 成功提取dayOfWeek(单值转List): {}", params.get("dayOfWeek"));
                }
            }
            if (contextMap.containsKey("timePeriod")) {
                Object timePeriod = contextMap.get("timePeriod");
                logger.info("[ConversationContext] 发现timePeriod: {} (类型: {})", timePeriod,
                        timePeriod != null ? timePeriod.getClass().getName() : "null");
                if (timePeriod instanceof Number) {
                    params.put("timePeriod", ((Number) timePeriod).intValue());
                    logger.info("[ConversationContext] 成功提取timePeriod: {}", params.get("timePeriod"));
                }
            }
            if (contextMap.containsKey("lastIntentType")) {
                params.put("lastIntentType", contextMap.get("lastIntentType"));
            }
            
            logger.info("[ConversationContext] 最终提取的参数: {}", params);
        }
        
        return context;
    }
    
    /**
     * 转换为 Map 用于保存
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        // 保存上次推荐的课程
        if (lastRecommendedCourses != null && !lastRecommendedCourses.isEmpty()) {
            List<Map<String, Object>> courses = new ArrayList<>();
            for (AgentResult.CourseInfo course : lastRecommendedCourses) {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("code", course.getCode());
                courseMap.put("name", course.getName());
                courseMap.put("credits", course.getCredits());
                courseMap.put("type", course.getType());
                courseMap.put("description", course.getDescription());
                courseMap.put("facultyName", course.getFacultyName());
                courseMap.put("teacherName", course.getTeacherName());
                courseMap.put("averageRating", course.getAverageRating());
                courseMap.put("reviewCount", course.getReviewCount());
                courses.add(courseMap);
            }
            map.put("lastRecommendedCourses", courses);
        }
        
        return map;
    }
    
    /**
     * 是否有上次推荐的课程
     */
    public boolean hasLastRecommendedCourses() {
        return lastRecommendedCourses != null && !lastRecommendedCourses.isEmpty();
    }
    
    /**
     * 获取对话历史摘要（最近5条）
     */
    public String getHistorySummary() {
        if (history == null || history.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, history.size() - 5);
        for (int i = start; i < history.size(); i++) {
            ChatRequest.MessageHistory msg = history.get(i);
            sb.append(msg.getRole().equals("user") ? "用户: " : "助手: ");
            String content = msg.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            sb.append(content).append("\n");
        }
        return sb.toString();
    }
}
