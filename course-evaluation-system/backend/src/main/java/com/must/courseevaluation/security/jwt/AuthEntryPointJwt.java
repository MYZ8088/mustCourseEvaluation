package com.must.courseevaluation.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        logger.error("未授权错误: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "未授权");
        body.put("message", translateAuthError(authException.getMessage()));
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
    
    // 将英文认证错误信息转换为中文
    private String translateAuthError(String errorMessage) {
        if (errorMessage == null) {
            return "请先登录";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("bad credentials")) {
            return "用户名或密码错误";
        }
        if (lowerMessage.contains("full authentication is required")) {
            return "请先登录后再访问";
        }
        if (lowerMessage.contains("access token expired") || lowerMessage.contains("jwt expired")) {
            return "登录已过期，请重新登录";
        }
        if (lowerMessage.contains("invalid") && lowerMessage.contains("token")) {
            return "无效的登录凭证，请重新登录";
        }
        if (lowerMessage.contains("cannot convert access token")) {
            return "登录凭证无效，请重新登录";
        }
        if (lowerMessage.contains("user not found") || lowerMessage.contains("username not found")) {
            return "用户不存在";
        }
        if (lowerMessage.contains("account is disabled") || lowerMessage.contains("user is disabled")) {
            return "账户已被禁用";
        }
        if (lowerMessage.contains("account is locked")) {
            return "账户已被锁定";
        }
        
        // 如果已经是中文，直接返回
        if (errorMessage.matches(".*[\\u4e00-\\u9fa5].*")) {
            return errorMessage;
        }
        
        return "访问被拒绝，请先登录";
    }
} 