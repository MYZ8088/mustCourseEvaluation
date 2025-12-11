package com.must.courseevaluation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 安全审计服务接口
 */
public interface SecurityAuditService {
    
    /**
     * 记录登录尝试
     * @param username 用户名
     * @param success 是否成功
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void logLoginAttempt(String username, boolean success, String ipAddress, String userAgent);
    
    /**
     * 记录安全事件
     * @param eventType 事件类型
     * @param description 事件描述
     * @param username 相关用户
     * @param ipAddress IP地址
     * @param severity 严重程度 (LOW, MEDIUM, HIGH, CRITICAL)
     */
    void logSecurityEvent(String eventType, String description, String username, String ipAddress, String severity);
    
    /**
     * 获取安全日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param eventType 事件类型过滤
     * @param limit 记录数限制
     * @return 安全日志列表
     */
    List<Map<String, Object>> getSecurityLogs(LocalDateTime startTime, LocalDateTime endTime, String eventType, int limit);
    
    /**
     * 获取失败登录统计
     * @param hours 过去多少小时
     * @return 失败登录统计
     */
    Map<String, Object> getFailedLoginStats(int hours);
    
    /**
     * 检查可疑活动
     * @return 可疑活动报告
     */
    Map<String, Object> detectSuspiciousActivity();
    
    /**
     * 清理过期日志
     * @param daysToKeep 保留天数
     * @return 清理结果
     */
    Map<String, Object> cleanupOldLogs(int daysToKeep);
} 