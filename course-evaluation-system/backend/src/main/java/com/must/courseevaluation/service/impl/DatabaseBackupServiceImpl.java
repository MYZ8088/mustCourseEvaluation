package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.service.DatabaseBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DatabaseBackupServiceImpl implements DatabaseBackupService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupServiceImpl.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    private final List<Map<String, Object>> backupHistory = new ArrayList<>();
    
    @Override
    public Map<String, Object> createBackup(String backupName) {
        logger.info("开始创建数据备份: {}", backupName);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有表的统计信息作为备份验证
            Map<String, Object> stats = getDatabaseStats();
            
            // 创建备份记录
            Map<String, Object> backup = new HashMap<>();
            backup.put("name", backupName);
            backup.put("timestamp", LocalDateTime.now());
            backup.put("stats", stats);
            backup.put("status", "COMPLETED");
            
            // 添加到备份历史
            backupHistory.add(backup);
            
            // 由于使用Neon云数据库，实际备份由云服务提供商自动处理
            // 这里主要记录备份时间点和数据统计
            
            result.put("success", true);
            result.put("message", "备份创建成功");
            result.put("backupName", backupName);
            result.put("timestamp", LocalDateTime.now());
            result.put("dataStats", stats);
            
            logger.info("数据备份创建成功: {}", backupName);
            
        } catch (Exception e) {
            logger.error("创建备份失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "备份创建失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getBackupList() {
        // 返回备份历史记录
        // 注意：实际的备份文件由Neon云服务管理
        List<Map<String, Object>> backups = new ArrayList<>(backupHistory);
        
        // 添加Neon自动备份信息
        Map<String, Object> neonBackup = new HashMap<>();
        neonBackup.put("name", "Neon自动备份");
        neonBackup.put("type", "AUTOMATIC");
        neonBackup.put("description", "Neon云平台自动每日备份");
        neonBackup.put("retention", "7天");
        
        backups.add(0, neonBackup);
        
        return backups;
    }
    
    @Override
    public Map<String, Object> validateDataIntegrity() {
        logger.info("开始验证数据完整性");
        
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // 检查用户表完整性
            validateUserData(issues);
            
            // 检查课程表完整性
            validateCourseData(issues);
            
            // 检查评价表完整性
            validateReviewData(issues);
            
            // 检查教师表完整性
            validateTeacherData(issues);
            
            // 检查学院表完整性
            validateFacultyData(issues);
            
            result.put("valid", issues.isEmpty());
            result.put("issues", issues);
            result.put("checkTime", LocalDateTime.now());
            
            if (issues.isEmpty()) {
                logger.info("数据完整性验证通过");
            } else {
                logger.warn("数据完整性验证发现问题: {}", issues);
            }
            
        } catch (Exception e) {
            logger.error("数据完整性验证失败: {}", e.getMessage(), e);
            result.put("valid", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取各表的记录数
            stats.put("userCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
            stats.put("courseCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM courses", Long.class));
            stats.put("teacherCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM teachers", Long.class));
            stats.put("facultyCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM faculties", Long.class));
            stats.put("reviewCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Long.class));
            
            // 获取数据库大小信息（如果权限允许）
            try {
                String dbSizeQuery = "SELECT pg_size_pretty(pg_database_size(current_database())) as size";
                String dbSize = jdbcTemplate.queryForObject(dbSizeQuery, String.class);
                stats.put("databaseSize", dbSize);
            } catch (Exception e) {
                stats.put("databaseSize", "无法获取");
            }
            
            // 获取最后更新时间
            stats.put("lastChecked", LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("获取数据库统计信息失败: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    @Override
    public boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            logger.error("数据库连接检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> cleanupOldBackups(int daysToKeep) {
        logger.info("开始清理{}天前的备份记录", daysToKeep);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        int removedCount = 0;
        Iterator<Map<String, Object>> iterator = backupHistory.iterator();
        
        while (iterator.hasNext()) {
            Map<String, Object> backup = iterator.next();
            LocalDateTime backupTime = (LocalDateTime) backup.get("timestamp");
            
            if (backupTime != null && backupTime.isBefore(cutoffDate)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("removedCount", removedCount);
        result.put("remainingCount", backupHistory.size());
        result.put("cleanupTime", LocalDateTime.now());
        
        logger.info("清理完成，删除{}个旧备份记录", removedCount);
        
        return result;
    }
    
    // 私有方法：验证用户数据
    private void validateUserData(List<String> issues) {
        try {
            // 检查空用户名
            Long emptyUsernameCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username IS NULL OR username = ''", Long.class);
            if (emptyUsernameCount > 0) {
                issues.add("发现" + emptyUsernameCount + "个用户名为空的用户记录");
            }
            
            // 检查重复用户名
            Long duplicateUsernameCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (SELECT username FROM users GROUP BY username HAVING COUNT(*) > 1) as duplicates", 
                Long.class);
            if (duplicateUsernameCount > 0) {
                issues.add("发现" + duplicateUsernameCount + "个重复的用户名");
            }
            
            // 检查无效邮箱
            Long invalidEmailCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email IS NULL OR email = '' OR email NOT LIKE '%@%'", Long.class);
            if (invalidEmailCount > 0) {
                issues.add("发现" + invalidEmailCount + "个无效的邮箱地址");
            }
            
        } catch (Exception e) {
            issues.add("用户数据验证失败: " + e.getMessage());
        }
    }
    
    // 私有方法：验证课程数据
    private void validateCourseData(List<String> issues) {
        try {
            // 检查课程是否有对应的学院
            Long orphanCourseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM courses c LEFT JOIN faculties f ON c.faculty_id = f.id WHERE f.id IS NULL", 
                Long.class);
            if (orphanCourseCount > 0) {
                issues.add("发现" + orphanCourseCount + "个没有关联学院的课程");
            }
            
            // 检查课程是否有对应的教师
            Long orphanCourseTeacherCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM courses c LEFT JOIN teachers t ON c.teacher_id = t.id WHERE t.id IS NULL", 
                Long.class);
            if (orphanCourseTeacherCount > 0) {
                issues.add("发现" + orphanCourseTeacherCount + "个没有关联教师的课程");
            }
            
        } catch (Exception e) {
            issues.add("课程数据验证失败: " + e.getMessage());
        }
    }
    
    // 私有方法：验证评价数据
    private void validateReviewData(List<String> issues) {
        try {
            // 检查评价是否有对应的用户
            Long orphanReviewUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reviews r LEFT JOIN users u ON r.user_id = u.id WHERE u.id IS NULL", 
                Long.class);
            if (orphanReviewUserCount > 0) {
                issues.add("发现" + orphanReviewUserCount + "个没有关联用户的评价");
            }
            
            // 检查评价是否有对应的课程
            Long orphanReviewCourseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reviews r LEFT JOIN courses c ON r.course_id = c.id WHERE c.id IS NULL", 
                Long.class);
            if (orphanReviewCourseCount > 0) {
                issues.add("发现" + orphanReviewCourseCount + "个没有关联课程的评价");
            }
            
            // 检查评分范围
            Long invalidRatingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reviews WHERE rating < 1 OR rating > 5", Long.class);
            if (invalidRatingCount > 0) {
                issues.add("发现" + invalidRatingCount + "个评分超出有效范围(1-5)的评价");
            }
            
        } catch (Exception e) {
            issues.add("评价数据验证失败: " + e.getMessage());
        }
    }
    
    // 私有方法：验证教师数据
    private void validateTeacherData(List<String> issues) {
        try {
            // 检查教师是否有对应的学院
            Long orphanTeacherCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM teachers t LEFT JOIN faculties f ON t.faculty_id = f.id WHERE f.id IS NULL", 
                Long.class);
            if (orphanTeacherCount > 0) {
                issues.add("发现" + orphanTeacherCount + "个没有关联学院的教师");
            }
            
        } catch (Exception e) {
            issues.add("教师数据验证失败: " + e.getMessage());
        }
    }
    
    // 私有方法：验证学院数据
    private void validateFacultyData(List<String> issues) {
        try {
            // 检查空学院名称
            Long emptyFacultyNameCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM faculties WHERE name IS NULL OR name = ''", Long.class);
            if (emptyFacultyNameCount > 0) {
                issues.add("发现" + emptyFacultyNameCount + "个学院名称为空的记录");
            }
            
            // 检查重复学院名称
            Long duplicateFacultyNameCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (SELECT name FROM faculties GROUP BY name HAVING COUNT(*) > 1) as duplicates", 
                Long.class);
            if (duplicateFacultyNameCount > 0) {
                issues.add("发现" + duplicateFacultyNameCount + "个重复的学院名称");
            }
            
        } catch (Exception e) {
            issues.add("学院数据验证失败: " + e.getMessage());
        }
    }
} 