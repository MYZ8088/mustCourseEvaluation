package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.CourseSummaryDto;

/**
 * 课程AI总结服务接口
 */
public interface CourseSummaryService {
    
    /**
     * 获取课程AI总结（优先返回缓存，如没有缓存且评论>=10则自动生成）
     * @param courseId 课程ID
     * @return 课程总结DTO，如果评论不足10条返回null
     */
    CourseSummaryDto getCourseSummary(Long courseId);
    
    /**
     * 强制重新生成课程AI总结（管理员专用）
     * @param courseId 课程ID
     * @return 课程总结DTO
     */
    CourseSummaryDto regenerateCourseSummary(Long courseId);
    
    /**
     * 检查AI服务是否可用
     * @return true如果可用
     */
    boolean isAIServiceAvailable();
    
    /**
     * 获取课程评论数量
     * @param courseId 课程ID
     * @return 评论数量
     */
    int getReviewCount(Long courseId);
}

