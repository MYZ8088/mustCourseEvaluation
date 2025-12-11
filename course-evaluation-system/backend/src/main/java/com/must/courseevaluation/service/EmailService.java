package com.must.courseevaluation.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendSimpleEmail(String to, String subject, String content);
    
    /**
     * 发送HTML格式邮件
     * @param to 收件人
     * @param subject 主题
     * @param content HTML内容
     */
    void sendHtmlEmail(String to, String subject, String content);
    
    /**
     * 发送验证码邮件（注册用）
     * @param to 收件人
     * @param code 验证码
     */
    void sendVerificationCodeEmail(String to, String code);
    
    /**
     * 发送登录验证码邮件
     * @param to 收件人
     * @param code 验证码
     */
    void sendLoginVerificationCodeEmail(String to, String code);
} 