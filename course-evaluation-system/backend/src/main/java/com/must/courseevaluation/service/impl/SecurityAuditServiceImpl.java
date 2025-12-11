package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class SecurityAuditServiceImpl implements SecurityAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditServiceImpl.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    
    // 使用线程安全的队列存储安全日志
    private final Queue<Map<String, Object>> securityLogs = new ConcurrentLinkedQueue<>();
    
    // 最大日志条数，防止内存溢出
    private static final int MAX_LOG_ENTRIES = 10000;
    
    @Override
    public void logLoginAttempt(String username, boolean success, String ipAddress, String userAgent) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("eventType", "LOGIN_ATTEMPT");
        logEntry.put("username", username);
        logEntry.put("success", success);
        logEntry.put("ipAddress", ipAddress);
        logEntry.put("userAgent", userAgent);
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("severity", success ? "LOW" : "MEDIUM");
        logEntry.put("description", success ? "登录成功" : "登录失败");
        
        addLogEntry(logEntry);
        
        // 记录到专门的安全日志
        if (success) {
            securityLogger.info("登录成功 - 用户: {}, IP: {}, UserAgent: {}", username, ipAddress, userAgent);
        } else {
            securityLogger.warn("登录失败 - 用户: {}, IP: {}, UserAgent: {}", username, ipAddress, userAgent);
        }
    }
    
    @Override
    public void logSecurityEvent(String eventType, String description, String username, String ipAddress, String severity) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("eventType", eventType);
        logEntry.put("description", description);
        logEntry.put("username", username);
        logEntry.put("ipAddress", ipAddress);
        logEntry.put("severity", severity);
        logEntry.put("timestamp", LocalDateTime.now());
        
        addLogEntry(logEntry);
        
        // 根据严重程度选择日志级别
        switch (severity.toUpperCase()) {
            case "CRITICAL":
            case "HIGH":
                securityLogger.error("安全事件 [{}] - {}, 用户: {}, IP: {}", severity, description, username, ipAddress);
                break;
            case "MEDIUM":
                securityLogger.warn("安全事件 [{}] - {}, 用户: {}, IP: {}", severity, description, username, ipAddress);
                break;
            default:
                securityLogger.info("安全事件 [{}] - {}, 用户: {}, IP: {}", severity, description, username, ipAddress);
        }
    }
    
    @Override
    public List<Map<String, Object>> getSecurityLogs(LocalDateTime startTime, LocalDateTime endTime, String eventType, int limit) {
        return securityLogs.stream()
                .filter(log -> {
                    LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
                    return (startTime == null || logTime.isAfter(startTime)) &&
                           (endTime == null || logTime.isBefore(endTime)) &&
                           (eventType == null || eventType.equals(log.get("eventType")));
                })
                .sorted((a, b) -> {
                    LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
                    LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
                    return timeB.compareTo(timeA); // 降序排列，最新的在前面
                })
                .limit(limit > 0 ? limit : 100)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getFailedLoginStats(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        List<Map<String, Object>> failedLogins = securityLogs.stream()
                .filter(log -> {
                    LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
                    return "LOGIN_ATTEMPT".equals(log.get("eventType")) &&
                           !((Boolean) log.get("success")) &&
                           logTime.isAfter(cutoffTime);
                })
                .collect(Collectors.toList());
        
        // 按IP地址统计失败次数
        Map<String, Long> failuresByIp = failedLogins.stream()
                .collect(Collectors.groupingBy(
                        log -> (String) log.get("ipAddress"),
                        Collectors.counting()
                ));
        
        // 按用户名统计失败次数
        Map<String, Long> failuresByUsername = failedLogins.stream()
                .collect(Collectors.groupingBy(
                        log -> (String) log.get("username"),
                        Collectors.counting()
                ));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFailedLogins", failedLogins.size());
        stats.put("timeRangeHours", hours);
        stats.put("failuresByIp", failuresByIp);
        stats.put("failuresByUsername", failuresByUsername);
        stats.put("reportTime", LocalDateTime.now());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> detectSuspiciousActivity() {
        Map<String, Object> report = new HashMap<>();
        List<String> suspiciousActivities = new ArrayList<>();
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        // 检查短时间内多次失败登录
        Map<String, Long> recentFailuresByIp = securityLogs.stream()
                .filter(log -> {
                    LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
                    return "LOGIN_ATTEMPT".equals(log.get("eventType")) &&
                           !((Boolean) log.get("success")) &&
                           logTime.isAfter(oneHourAgo);
                })
                .collect(Collectors.groupingBy(
                        log -> (String) log.get("ipAddress"),
                        Collectors.counting()
                ));
        
        // 检查是否有IP地址在1小时内失败登录超过5次
        recentFailuresByIp.entrySet().stream()
                .filter(entry -> entry.getValue() >= 5)
                .forEach(entry -> {
                    suspiciousActivities.add(String.format("IP地址 %s 在过去1小时内失败登录 %d 次", 
                            entry.getKey(), entry.getValue()));
                });
        
        // 检查是否有用户在短时间内从多个IP登录
        Map<String, Set<String>> userIpMap = securityLogs.stream()
                .filter(log -> {
                    LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
                    return "LOGIN_ATTEMPT".equals(log.get("eventType")) &&
                           ((Boolean) log.get("success")) &&
                           logTime.isAfter(oneHourAgo);
                })
                .collect(Collectors.groupingBy(
                        log -> (String) log.get("username"),
                        Collectors.mapping(
                                log -> (String) log.get("ipAddress"),
                                Collectors.toSet()
                        )
                ));
        
        userIpMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 3)
                .forEach(entry -> {
                    suspiciousActivities.add(String.format("用户 %s 在过去1小时内从 %d 个不同IP地址登录", 
                            entry.getKey(), entry.getValue().size()));
                });
        
        // 检查高严重程度安全事件
        long highSeverityEvents = securityLogs.stream()
                .filter(log -> {
                    LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
                    String severity = (String) log.get("severity");
                    return logTime.isAfter(oneDayAgo) &&
                           ("HIGH".equals(severity) || "CRITICAL".equals(severity));
                })
                .count();
        
        if (highSeverityEvents > 0) {
            suspiciousActivities.add(String.format("过去24小时内发现 %d 个高严重程度安全事件", highSeverityEvents));
        }
        
        report.put("suspiciousActivities", suspiciousActivities);
        report.put("riskLevel", determinRiskLevel(suspiciousActivities.size()));
        report.put("checkTime", LocalDateTime.now());
        report.put("totalSuspiciousCount", suspiciousActivities.size());
        
        if (!suspiciousActivities.isEmpty()) {
            logger.warn("检测到可疑活动: {}", suspiciousActivities);
        }
        
        return report;
    }
    
    @Override
    public Map<String, Object> cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        
        int originalSize = securityLogs.size();
        securityLogs.removeIf(log -> {
            LocalDateTime logTime = (LocalDateTime) log.get("timestamp");
            return logTime.isBefore(cutoffTime);
        });
        
        int removedCount = originalSize - securityLogs.size();
        
        Map<String, Object> result = new HashMap<>();
        result.put("removedCount", removedCount);
        result.put("remainingCount", securityLogs.size());
        result.put("cleanupTime", LocalDateTime.now());
        result.put("daysKept", daysToKeep);
        
        logger.info("安全日志清理完成，删除 {} 条日志，保留 {} 条", removedCount, securityLogs.size());
        
        return result;
    }
    
    // 私有方法：添加日志条目
    private void addLogEntry(Map<String, Object> logEntry) {
        securityLogs.offer(logEntry);
        
        // 如果日志条数超过限制，删除最旧的条目
        while (securityLogs.size() > MAX_LOG_ENTRIES) {
            securityLogs.poll();
        }
    }
    
    // 私有方法：根据可疑活动数量确定风险级别
    private String determinRiskLevel(int suspiciousCount) {
        if (suspiciousCount == 0) {
            return "LOW";
        } else if (suspiciousCount <= 2) {
            return "MEDIUM";
        } else if (suspiciousCount <= 5) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }
} 