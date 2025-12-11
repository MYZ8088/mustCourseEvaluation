package com.must.courseevaluation.service.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图分类结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {
    
    /**
     * 意图类型
     */
    private IntentType intentType;
    
    /**
     * 提取的参数
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * 是否引用上次推荐结果
     */
    @Builder.Default
    private boolean referenceLastResult = false;
    
    /**
     * 提取的关键词
     */
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    
    /**
     * 置信度 (0-1)
     */
    @Builder.Default
    private double confidence = 0.0;
    
    /**
     * 原始用户消息
     */
    private String originalMessage;
    
    /**
     * 需要比较的课程名称（COMPARE类型使用）
     */
    @Builder.Default
    private List<String> coursesToCompare = new ArrayList<>();
    
    /**
     * 查询的课程名称（DETAIL类型使用）
     */
    private String courseToQuery;
}

