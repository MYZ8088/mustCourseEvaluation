package com.must.courseevaluation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                translateErrorMessage(exception.getMessage()),
                request.getDescription(false),
                HttpStatus.NOT_FOUND.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = translateValidationError(fieldName, error.getDefaultMessage());
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                translateErrorMessage(exception.getMessage()),
                request.getDescription(false),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(
            BadCredentialsException exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "用户名或密码错误",
                request.getDescription(false),
                HttpStatus.UNAUTHORIZED.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                translateAuthError(exception.getMessage()),
                request.getDescription(false),
                HttpStatus.UNAUTHORIZED.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "访问被拒绝，您没有权限执行此操作",
                request.getDescription(false),
                HttpStatus.FORBIDDEN.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception exception, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                translateErrorMessage(exception.getMessage()),
                request.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // 翻译通用错误信息
    private String translateErrorMessage(String message) {
        if (message == null) {
            return "操作失败，请稍后重试";
        }
        
        String lowerMessage = message.toLowerCase();
        
        // 常见英文错误消息翻译
        if (lowerMessage.contains("not found")) {
            return "请求的资源不存在";
        }
        if (lowerMessage.contains("already exists")) {
            return "数据已存在";
        }
        if (lowerMessage.contains("connection refused")) {
            return "服务连接失败，请稍后重试";
        }
        if (lowerMessage.contains("timeout")) {
            return "请求超时，请稍后重试";
        }
        if (lowerMessage.contains("internal server error")) {
            return "服务器内部错误";
        }
        
        // 如果已经是中文，直接返回
        if (message.matches(".*[\\u4e00-\\u9fa5].*")) {
            return message;
        }
        
        return "操作失败: " + message;
    }
    
    // 翻译认证相关错误
    private String translateAuthError(String message) {
        if (message == null) {
            return "认证失败";
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("bad credentials")) {
            return "用户名或密码错误";
        }
        if (lowerMessage.contains("user not found")) {
            return "用户不存在";
        }
        if (lowerMessage.contains("account is disabled")) {
            return "账户已被禁用";
        }
        if (lowerMessage.contains("account is locked")) {
            return "账户已被锁定";
        }
        if (lowerMessage.contains("credentials expired")) {
            return "密码已过期";
        }
        
        if (message.matches(".*[\\u4e00-\\u9fa5].*")) {
            return message;
        }
        
        return "认证失败: " + message;
    }
    
    // 翻译验证错误字段名
    private String translateValidationError(String fieldName, String message) {
        if (message == null) {
            return "验证失败";
        }
        
        // 翻译常见字段名
        Map<String, String> fieldNameMap = Map.of(
            "username", "用户名",
            "password", "密码",
            "email", "电子邮箱",
            "content", "内容",
            "rating", "评分",
            "title", "标题",
            "name", "名称"
        );
        
        String chineseFieldName = fieldNameMap.getOrDefault(fieldName, fieldName);
        
        // 翻译常见验证消息
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("must not be blank") || lowerMessage.contains("must not be empty")) {
            return chineseFieldName + "不能为空";
        }
        if (lowerMessage.contains("must not be null")) {
            return chineseFieldName + "不能为空";
        }
        if (lowerMessage.contains("size must be between")) {
            return chineseFieldName + "长度不符合要求";
        }
        if (lowerMessage.contains("must be a well-formed email")) {
            return "请输入有效的电子邮箱地址";
        }
        if (lowerMessage.contains("must be greater than") || lowerMessage.contains("must be at least")) {
            return chineseFieldName + "数值过小";
        }
        if (lowerMessage.contains("must be less than")) {
            return chineseFieldName + "数值过大";
        }
        
        // 如果已经是中文，直接返回
        if (message.matches(".*[\\u4e00-\\u9fa5].*")) {
            return message;
        }
        
        return message;
    }
} 