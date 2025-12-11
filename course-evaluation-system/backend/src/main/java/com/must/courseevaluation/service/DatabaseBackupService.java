package com.must.courseevaluation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据库备份服务接口
 */
public interface DatabaseBackupService {
    
    /**
     * 创建数据备份
     * @param backupName 备份名称
     * @return 备份结果信息
     */
    Map<String, Object> createBackup(String backupName);
    
    /**
     * 获取备份列表
     * @return 备份列表
     */
    List<Map<String, Object>> getBackupList();
    
    /**
     * 验证数据完整性
     * @return 验证结果
     */
    Map<String, Object> validateDataIntegrity();
    
    /**
     * 获取数据库统计信息
     * @return 统计信息
     */
    Map<String, Object> getDatabaseStats();
    
    /**
     * 检查数据库连接状态
     * @return 连接状态
     */
    boolean checkDatabaseConnection();
    
    /**
     * 清理过期备份
     * @param daysToKeep 保留天数
     * @return 清理结果
     */
    Map<String, Object> cleanupOldBackups(int daysToKeep);
} 