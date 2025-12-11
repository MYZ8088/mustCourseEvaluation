package com.must.courseevaluation.service.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 路由器
 * 根据意图类型分发到对应的处理 Agent
 */
@Component
public class AgentRouter {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentRouter.class);
    
    private final Map<IntentType, BaseAgent> agentMap = new HashMap<>();
    private final ResponseAgent responseAgent;
    
    public AgentRouter(
            NewQueryAgent newQueryAgent,
            RefineAgent refineAgent,
            SupplementAgent supplementAgent,
            CompareAgent compareAgent,
            DetailAgent detailAgent,
            ChatAgent chatAgent,
            ResponseAgent responseAgent) {
        
        this.responseAgent = responseAgent;
        
        // 注册所有 Agent
        agentMap.put(IntentType.NEW_QUERY, newQueryAgent);
        agentMap.put(IntentType.REFINE, refineAgent);
        agentMap.put(IntentType.SUPPLEMENT, supplementAgent);
        agentMap.put(IntentType.COMPARE, compareAgent);
        agentMap.put(IntentType.DETAIL, detailAgent);
        agentMap.put(IntentType.CHAT, chatAgent);
        
        logger.info("AgentRouter 初始化完成，注册了 {} 个 Agent", agentMap.size());
    }
    
    /**
     * 路由请求到对应的 Agent 处理
     */
    public AgentResult route(IntentResult intent, ConversationContext context) {
        IntentType intentType = intent.getIntentType();
        logger.info("[AgentRouter] 路由意图: {} -> {}", intent.getOriginalMessage(), intentType);
        
        BaseAgent agent = agentMap.get(intentType);
        
        if (agent == null) {
            logger.warn("[AgentRouter] 未找到对应的 Agent: {}", intentType);
            // 降级为新查询
            agent = agentMap.get(IntentType.NEW_QUERY);
        }
        
        // 处理请求
        AgentResult result = agent.process(intent, context);
        
        // 如果有课程推荐结果，生成话术
        if (result.isSuccess() && result.getCourses() != null && !result.getCourses().isEmpty()) {
            String response = responseAgent.generateRecommendationResponse(result, intent, context);
            result.setMessage(response);
            
            // 更新上下文中的上次推荐课程
            if (result.getUpdatedContext() != null) {
                result.getUpdatedContext().put("lastRecommendedCourses", 
                    convertCoursesToMap(result.getCourses()));
            }
        }
        
        return result;
    }
    
    /**
     * 将 CourseInfo 列表转换为 Map 列表（用于保存到上下文）
     */
    private List<Map<String, Object>> convertCoursesToMap(List<AgentResult.CourseInfo> courses) {
        return courses.stream()
                .map(course -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", course.getId());
                    map.put("code", course.getCode());
                    map.put("name", course.getName());
                    map.put("credits", course.getCredits());
                    map.put("type", course.getType());
                    map.put("description", course.getDescription());
                    map.put("facultyName", course.getFacultyName());
                    map.put("teacherName", course.getTeacherName());
                    map.put("averageRating", course.getAverageRating());
                    map.put("reviewCount", course.getReviewCount());
                    return map;
                })
                .toList();
    }
    
    /**
     * 获取 ResponseAgent（用于外部调用）
     */
    public ResponseAgent getResponseAgent() {
        return responseAgent;
    }
}

