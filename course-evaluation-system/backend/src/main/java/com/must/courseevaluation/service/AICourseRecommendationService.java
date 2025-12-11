package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.AIRecommendationResponse;
import com.must.courseevaluation.dto.ChatRequest;

/**
 * AI课程推荐服务接口
 * 
 * 功能：
 * 1. 接收用户自然语言消息
 * 2. 调用DeepSeek API解析用户意图
 * 3. 使用规则引擎过滤匹配的课程
 * 4. 调用DeepSeek API生成推荐话术
 * 5. 返回结构化的推荐结果
 */
public interface AICourseRecommendationService {
    
    /**
     * 处理用户消息并返回AI推荐结果
     * 
     * @param request 包含用户消息和上下文的请求
     * @return AI推荐响应（文本或课程推荐）
     */
    AIRecommendationResponse processMessage(ChatRequest request);
    
    /**
     * 检查AI服务是否可用
     * 
     * @return true如果AI服务已配置且可用
     */
    boolean isAIServiceAvailable();
}

