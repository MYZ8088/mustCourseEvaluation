package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.service.VerificationCodeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    // 验证码有效期（分钟）
    private static final int EXPIRATION_MINUTES = 10;
    
    // 存储验证码的Map: key是邮箱，value是包含验证码和过期时间的对象
    private final Map<String, VerificationCodeInfo> verificationCodes = new ConcurrentHashMap<>();
    
    // 清理过期验证码的调度器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public VerificationCodeServiceImpl() {
        // 每分钟执行一次清理过期验证码的任务
        scheduler.scheduleAtFixedRate(this::cleanExpiredCodes, 1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public String generateVerificationCode() {
        // 生成6位数字验证码
        return String.format("%06d", new Random().nextInt(1000000));
    }
    
    @Override
    public void storeVerificationCode(String email, String code) {
        // 计算过期时间
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        
        // 存储验证码和过期时间
        verificationCodes.put(email, new VerificationCodeInfo(code, expirationTime));
    }
    
    @Override
    public boolean validateVerificationCode(String email, String code) {
        // 管理员特殊标识，绕过邮箱验证
        if ("ADMIN_CREATE".equals(code)) {
            return true;
        }
        
        // 获取存储的验证码信息
        VerificationCodeInfo storedInfo = verificationCodes.get(email);
        
        // 如果没有找到验证码或验证码已过期，返回false
        if (storedInfo == null || storedInfo.isExpired()) {
            return false;
        }
        
        // 验证码是否匹配
        boolean isValid = storedInfo.getCode().equals(code);
        
        // 如果验证成功，移除验证码
        if (isValid) {
            verificationCodes.remove(email);
        }
        
        return isValid;
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 验证码信息类，包含验证码和过期时间
     */
    private static class VerificationCodeInfo {
        private final String code;
        private final LocalDateTime expirationTime;
        
        public VerificationCodeInfo(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }
    
    /**
     * 关闭调度器
     */
    public void shutdownScheduler() {
        scheduler.shutdown();
    }
    
    /**
     * Bean销毁时调用
     */
    @jakarta.annotation.PreDestroy
    public void preDestroy() {
        shutdownScheduler();
    }
} 