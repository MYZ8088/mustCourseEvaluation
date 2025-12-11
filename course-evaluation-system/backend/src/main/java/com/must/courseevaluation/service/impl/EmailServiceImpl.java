package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            logger.info("简单邮件已发送至: {}", to);
        } catch (Exception e) {
            logger.error("发送简单邮件时出错: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("HTML邮件已发送至: {}", to);
        } catch (MessagingException e) {
            logger.error("发送HTML邮件时出错: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    @Override
    public void sendVerificationCodeEmail(String to, String code) {
        String subject = "课程评价系统 - 邮箱验证码";
        String htmlContent = "<div style='background-color: #f4f7f9; padding: 20px; font-family: Arial, sans-serif;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 3px 6px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; margin-top: 0;'>邮箱验证码</h2>"
                + "<p style='color: #555; font-size: 16px;'>尊敬的用户：</p>"
                + "<p style='color: #555; font-size: 16px;'>您好！您正在注册课程评价系统账号，请使用以下验证码完成邮箱验证：</p>"
                + "<div style='background-color: #f2f4f6; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;'>"
                + "<span style='color: #1e88e5; font-size: 24px; font-weight: bold; letter-spacing: 5px;'>" + code + "</span>"
                + "</div>"
                + "<p style='color: #555; font-size: 16px;'>验证码有效期为10分钟，请勿将验证码泄露给他人。</p>"
                + "<p style='color: #555; font-size: 16px;'>如非本人操作，请忽略此邮件。</p>"
                + "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 13px;'>"
                + "<p>这是一封自动发送的邮件，请勿直接回复。</p>"
                + "<p>© " + java.time.Year.now().getValue() + " 课程评价系统</p>"
                + "</div>"
                + "</div>"
                + "</div>";
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    @Override
    public void sendLoginVerificationCodeEmail(String to, String code) {
        String subject = "课程评价系统 - 登录验证码";
        String htmlContent = "<div style='background-color: #f4f7f9; padding: 20px; font-family: Arial, sans-serif;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 3px 6px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; margin-top: 0;'>登录验证码</h2>"
                + "<p style='color: #555; font-size: 16px;'>尊敬的用户：</p>"
                + "<p style='color: #555; font-size: 16px;'>您好！您正在使用邮箱验证码登录课程评价系统，请使用以下验证码完成登录：</p>"
                + "<div style='background-color: #f2f4f6; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;'>"
                + "<span style='color: #1e88e5; font-size: 24px; font-weight: bold; letter-spacing: 5px;'>" + code + "</span>"
                + "</div>"
                + "<p style='color: #555; font-size: 16px;'>验证码有效期为10分钟，请勿将验证码泄露给他人。</p>"
                + "<p style='color: #555; font-size: 16px;'>如非本人操作，请注意账户安全。</p>"
                + "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 13px;'>"
                + "<p>这是一封自动发送的邮件，请勿直接回复。</p>"
                + "<p>© " + java.time.Year.now().getValue() + " 课程评价系统</p>"
                + "</div>"
                + "</div>"
                + "</div>";
        
        sendHtmlEmail(to, subject, htmlContent);
    }
} 