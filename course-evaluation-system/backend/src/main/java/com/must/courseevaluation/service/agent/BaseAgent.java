package com.must.courseevaluation.service.agent;

/**
 * Agent 基础接口
 */
public interface BaseAgent {
    
    /**
     * 处理用户请求
     * 
     * @param intent 分类后的意图
     * @param context 对话上下文
     * @return 处理结果
     */
    AgentResult process(IntentResult intent, ConversationContext context);
    
    /**
     * 获取 Agent 支持的意图类型
     */
    IntentType getSupportedIntentType();
}

