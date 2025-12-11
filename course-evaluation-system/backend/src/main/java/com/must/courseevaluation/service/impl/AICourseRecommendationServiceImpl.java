package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.AIRecommendationResponse;
import com.must.courseevaluation.dto.AIRecommendationResponse.CourseRecommendation;
import com.must.courseevaluation.dto.ChatRequest;
import com.must.courseevaluation.service.AICourseRecommendationService;
import com.must.courseevaluation.service.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI课程推荐服务实现
 * 
 * Agent 架构：
 * 1. IntentClassifierAgent - 意图分类
 * 2. AgentRouter - 路由分发到对应 Agent
 * 3. 各类型 Agent - NewQuery, Refine, Supplement, Compare, Detail, Chat
 * 4. ResponseAgent - 话术生成
 */
@Service
public class AICourseRecommendationServiceImpl implements AICourseRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(AICourseRecommendationServiceImpl.class);

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.enabled}")
    private boolean enabled;

    private final IntentClassifierAgent intentClassifier;
    private final AgentRouter agentRouter;

    public AICourseRecommendationServiceImpl(
            IntentClassifierAgent intentClassifier,
            AgentRouter agentRouter) {
        this.intentClassifier = intentClassifier;
        this.agentRouter = agentRouter;
    }

    @Override
    public boolean isAIServiceAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty() && !apiKey.equals("sk-your-api-key-here");
    }

    @Override
    public AIRecommendationResponse processMessage(ChatRequest request) {
        String userMessage = request.getMessage();
        Map<String, Object> contextMap = request.getContext() != null ? request.getContext() : new HashMap<>();
        List<ChatRequest.MessageHistory> conversationHistory = request.getConversationHistory();

        logger.info("========== 开始处理用户消息 ==========");
        logger.info("[消息] {}", userMessage);
        logger.info("[历史条数] {}", conversationHistory != null ? conversationHistory.size() : 0);
        logger.info("[上下文] {}", contextMap);

        // 检查AI服务是否可用
        if (!isAIServiceAvailable()) {
            logger.warn("AI服务未配置或未启用");
            return AIRecommendationResponse.error("AI服务未配置或未启用，请联系管理员");
        }

        try {
            // ========== 第一步：构建对话上下文 ==========
            logger.info("[Step 1] 构建对话上下文...");
            logger.info("[接收的contextMap] {}", contextMap);  // 打印完整接收的上下文
            
            ConversationContext context = ConversationContext.fromMap(
                    contextMap, 
                    conversationHistory, 
                    request.getConversationId()
            );
            logger.info("[上下文] 上次推荐课程数: {}", 
                    context.hasLastRecommendedCourses() ? context.getLastRecommendedCourses().size() : 0);
            logger.info("[上下文] 解析后的参数: {}", context.getParameters());  // 打印解析后的参数
            
            // ========== 第二步：意图分类 ==========
            logger.info("[Step 2] 意图分类...");
            IntentResult intent = intentClassifier.classify(userMessage, context);
            logger.info("[意图] 类型={}, 置信度={}, 引用上次={}", 
                    intent.getIntentType(), intent.getConfidence(), intent.isReferenceLastResult());
            logger.info("[参数] {}", intent.getParameters());
            logger.info("[关键词] {}", intent.getKeywords());

            // ========== 第三步：路由到对应 Agent 处理 ==========
            logger.info("[Step 3] 路由到 {} Agent...", intent.getIntentType());
            AgentResult agentResult = agentRouter.route(intent, context);
            logger.info("[结果] 成功={}, 课程数={}", 
                    agentResult.isSuccess(), 
                    agentResult.getCourses() != null ? agentResult.getCourses().size() : 0);

            // ========== 第四步：构建响应 ==========
            logger.info("[Step 4] 构建响应...");
            return buildResponse(agentResult);

        } catch (Exception e) {
            logger.error("AI推荐处理失败: {}", e.getMessage(), e);
            return AIRecommendationResponse.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 根据 Agent 结果构建响应
     */
    private AIRecommendationResponse buildResponse(AgentResult result) {
        if (!result.isSuccess()) {
            return AIRecommendationResponse.error(result.getMessage());
        }

        // 处理不同意图类型的响应
        switch (result.getIntentType()) {
            case NEW_QUERY:
            case REFINE:
            case SUPPLEMENT:
                return buildRecommendationResponse(result);
                
            case COMPARE:
            case DETAIL:
            case CHAT:
                return buildTextResponse(result);
                
            default:
                return AIRecommendationResponse.text(result.getMessage());
        }
    }

    /**
     * 构建课程推荐响应
     */
    private AIRecommendationResponse buildRecommendationResponse(AgentResult result) {
        if (result.getCourses() == null || result.getCourses().isEmpty()) {
            AIRecommendationResponse response = AIRecommendationResponse.text(result.getMessage());
            response.setUpdatedContext(result.getUpdatedContext());
            return response;
        }

        // 转换课程格式
        List<CourseRecommendation> recommendations = result.getCourses().stream()
                .map(this::convertToCourseRecommendation)
                .collect(Collectors.toList());

        AIRecommendationResponse response = AIRecommendationResponse.recommendation(
                result.getMessage(), 
                recommendations
        );
        response.setUpdatedContext(result.getUpdatedContext());
        
        return response;
    }

    /**
     * 构建文本响应（比较、详情、闲聊等）
     */
    private AIRecommendationResponse buildTextResponse(AgentResult result) {
        AIRecommendationResponse response;
        
        if (result.getIntentType() == IntentType.DETAIL && result.getCourseDetail() != null) {
            // 详情查询：包含课程信息
            List<CourseRecommendation> courses = List.of(
                    convertToCourseRecommendation(result.getCourseDetail())
            );
            response = AIRecommendationResponse.recommendation(result.getMessage(), courses);
        } else {
            // 其他：纯文本
            response = AIRecommendationResponse.text(result.getMessage());
        }
        
        response.setUpdatedContext(result.getUpdatedContext());
        return response;
    }

    /**
     * 转换为课程推荐DTO
     */
    private CourseRecommendation convertToCourseRecommendation(AgentResult.CourseInfo courseInfo) {
        CourseRecommendation rec = new CourseRecommendation();
        rec.setId(courseInfo.getId());
        rec.setCode(courseInfo.getCode());
        rec.setName(courseInfo.getName());
        rec.setCredits(courseInfo.getCredits());
        rec.setType(courseInfo.getType());
        rec.setDescription(courseInfo.getDescription());
        rec.setFacultyName(courseInfo.getFacultyName());
        rec.setTeacherName(courseInfo.getTeacherName());
        rec.setAverageRating(courseInfo.getAverageRating());
        rec.setReviewCount(courseInfo.getReviewCount());
        rec.setReason(courseInfo.getReason());
        return rec;
    }
}
