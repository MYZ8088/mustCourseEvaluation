package com.must.courseevaluation.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 生成验证码
     * @return 生成的验证码
     */
    String generateVerificationCode();
    
    /**
     * 存储验证码
     * @param email 邮箱
     * @param code 验证码
     */
    void storeVerificationCode(String email, String code);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 验证结果
     */
    boolean validateVerificationCode(String email, String code);
} 