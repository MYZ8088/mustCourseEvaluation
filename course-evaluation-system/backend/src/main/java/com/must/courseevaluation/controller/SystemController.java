package com.must.courseevaluation.controller;

import com.must.courseevaluation.service.DatabaseBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/system")
public class SystemController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);
    
    @Autowired
    private DatabaseBackupService databaseBackupService;
    
    /**
     * 系统健康状态检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查数据库连接
            boolean dbConnected = databaseBackupService.checkDatabaseConnection();
            health.put("database", dbConnected ? "UP" : "DOWN");
            
            // 获取数据库统计信息
            Map<String, Object> dbStats = databaseBackupService.getDatabaseStats();
            health.put("databaseStats", dbStats);
            
            // 系统整体状态
            health.put("status", dbConnected ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("系统健康检查失败: {}", e.getMessage(), e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(health);
        }
    }
    
    /**
     * 创建数据备份 - 管理员专用
     */
    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBackup(@RequestParam(required = false) String name) {
        try {
            String backupName = name != null ? name : "manual_backup_" + System.currentTimeMillis();
            Map<String, Object> result = databaseBackupService.createBackup(backupName);
            
            if ((Boolean) result.get("success")) {
                logger.info("管理员创建数据备份成功: {}", backupName);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(500).body(result);
            }
            
        } catch (Exception e) {
            logger.error("创建备份失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "备份创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取备份列表 - 管理员专用
     */
    @GetMapping("/backup/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getBackupList() {
        try {
            List<Map<String, Object>> backups = databaseBackupService.getBackupList();
            return ResponseEntity.ok(backups);
            
        } catch (Exception e) {
            logger.error("获取备份列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * 数据完整性验证 - 管理员专用
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> validateDataIntegrity() {
        try {
            Map<String, Object> result = databaseBackupService.validateDataIntegrity();
            
            if ((Boolean) result.get("valid")) {
                logger.info("数据完整性验证通过");
                return ResponseEntity.ok(result);
            } else {
                logger.warn("数据完整性验证发现问题: {}", result.get("issues"));
                return ResponseEntity.status(200).body(result); // 返回200但包含问题信息
            }
            
        } catch (Exception e) {
            logger.error("数据完整性验证失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 清理过期备份 - 管理员专用
     */
    @DeleteMapping("/backup/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldBackups(@RequestParam(defaultValue = "7") int daysToKeep) {
        try {
            Map<String, Object> result = databaseBackupService.cleanupOldBackups(daysToKeep);
            logger.info("备份清理完成: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("备份清理失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "备份清理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取数据库统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        try {
            Map<String, Object> stats = databaseBackupService.getDatabaseStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("获取数据库统计信息失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");
            jvm.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
            jvm.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
            jvm.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB");
            
            info.put("jvm", jvm);
            
            // 系统信息
            Map<String, Object> system = new HashMap<>();
            system.put("osName", System.getProperty("os.name"));
            system.put("osVersion", System.getProperty("os.version"));
            system.put("javaVersion", System.getProperty("java.version"));
            system.put("timestamp", LocalDateTime.now());
            
            info.put("system", system);
            
            // 应用信息
            Map<String, Object> app = new HashMap<>();
            app.put("name", "Course Evaluation System");
            app.put("version", "1.0.0");
            app.put("profile", System.getProperty("spring.profiles.active", "default"));
            
            info.put("application", app);
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("获取系统信息失败: {}", e.getMessage(), e);
            info.put("error", e.getMessage());
            return ResponseEntity.status(500).body(info);
        }
    }
} 