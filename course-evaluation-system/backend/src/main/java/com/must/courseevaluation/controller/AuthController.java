package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.JwtResponse;
import com.must.courseevaluation.dto.auth.EmailLoginRequest;
import com.must.courseevaluation.dto.auth.LoginRequest;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.security.UserDetailsImpl;
import com.must.courseevaluation.security.jwt.JwtUtils;
import com.must.courseevaluation.service.EmailService;
import com.must.courseevaluation.service.SecurityAuditService;
import com.must.courseevaluation.service.UserService;
import com.must.courseevaluation.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SecurityAuditService securityAuditService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("尝试登录用户: {} 来自IP: {}", loginRequest.getUsername(), ipAddress);
        
        try {
            // 先检查用户是否存在及状态
            try {
                User user = userService.findByUsername(loginRequest.getUsername());
                if (!user.isActive()) {
                    logger.warn("用户尝试登录但账户已被停用: {}", loginRequest.getUsername());
                    
                    // 记录安全事件
                    securityAuditService.logSecurityEvent("ACCOUNT_DISABLED_LOGIN_ATTEMPT", 
                            "尝试使用已停用账户登录", loginRequest.getUsername(), ipAddress, "MEDIUM");
                    
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "账户已被停用，请联系管理员");
                    return ResponseEntity.status(403).body(response);
                }
            } catch (UsernameNotFoundException e) {
                // 用户不存在的情况下，继续走正常认证流程，会抛出相应的认证异常
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User.Role role = User.Role.valueOf(userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .findFirst().orElse(User.Role.ROLE_STUDENT.name()));
            
            // 记录成功登录
            securityAuditService.logLoginAttempt(loginRequest.getUsername(), true, ipAddress, userAgent);
            
            logger.info("用户登录成功: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), 
                    userDetails.getEmail(), role));
        } catch (Exception e) {
            // 记录失败登录
            securityAuditService.logLoginAttempt(loginRequest.getUsername(), false, ipAddress, userAgent);
            
            logger.error("登录失败: {}", e.getMessage());
            // 将英文错误信息转换为中文
            String errorMessage = translateAuthError(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("尝试注册新用户: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 用户名已被使用!"));
        }

        // 检查电子邮件是否已存在
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 电子邮件已被使用!"));
        }
        
        // 首先检查是否是管理员特殊标识
        boolean isAdminCreate = "ADMIN_CREATE".equals(registerRequest.getEmailCode());
        
        // 非管理员创建的用户需要验证邮箱后缀
        if (!isAdminCreate && !isValidStudentEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 请使用澳门科技大学学生邮箱 (@student.must.edu.mo) 注册!"));
        }
        
        // 只有非管理员创建的用户才需要验证邮箱验证码
        if (!isAdminCreate && (registerRequest.getEmailCode() == null || !validateEmailCode(registerRequest.getEmail(), registerRequest.getEmailCode()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 邮箱验证码无效或已过期!"));
        }

        // 创建新用户
        try {
            UserDto userDto = userService.register(registerRequest);
            logger.info("用户注册成功: {}", registerRequest.getUsername());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("用户创建失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "用户创建失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-email-verification")
    public ResponseEntity<?> sendEmailVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 电子邮箱不能为空!"));
        }
        
        // 验证邮箱后缀
        if (!isValidStudentEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 请使用澳门科技大学学生邮箱 (@student.must.edu.mo)!"));
        }
        
        try {
            // 生成随机验证码
            String verificationCode = verificationCodeService.generateVerificationCode();
            
            // 存储验证码
            verificationCodeService.storeVerificationCode(email, verificationCode);
            
            // 发送验证码邮件
            emailService.sendVerificationCodeEmail(email, verificationCode);
            
            logger.info("验证码已发送到邮箱: {}", email);
            
            return ResponseEntity.ok(Map.of("message", "验证码已发送，请查收邮箱"));
        } catch (Exception e) {
            logger.error("发送验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "发送验证码失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-login-verification")
    public ResponseEntity<?> sendLoginVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 电子邮箱不能为空!"));
        }
        
        // 检查邮箱是否已注册
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 该邮箱尚未注册，请先注册账号!"));
        }
        
        try {
            // 生成随机验证码
            String verificationCode = verificationCodeService.generateVerificationCode();
            
            // 存储验证码
            verificationCodeService.storeVerificationCode(email, verificationCode);
            
            // 发送登录验证码邮件
            emailService.sendLoginVerificationCodeEmail(email, verificationCode);
            
            logger.info("登录验证码已发送到邮箱: {}", email);
            
            return ResponseEntity.ok(Map.of("message", "验证码已发送，请查收邮箱"));
        } catch (Exception e) {
            logger.error("发送登录验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "发送验证码失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login-by-email")
    public ResponseEntity<?> loginByEmail(@Valid @RequestBody EmailLoginRequest emailLoginRequest, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("尝试邮箱验证码登录: {} 来自IP: {}", emailLoginRequest.getEmail(), ipAddress);
        
        try {
            // 验证邮箱验证码
            if (!validateEmailCode(emailLoginRequest.getEmail(), emailLoginRequest.getEmailCode())) {
                logger.warn("邮箱验证码无效: {}", emailLoginRequest.getEmail());
                securityAuditService.logLoginAttempt(emailLoginRequest.getEmail(), false, ipAddress, userAgent);
                return ResponseEntity.badRequest().body(Map.of("message", "验证码无效或已过期"));
            }
            
            // 通过邮箱查找用户
            User user = userService.findByEmail(emailLoginRequest.getEmail());
            if (user == null) {
                logger.warn("邮箱未注册: {}", emailLoginRequest.getEmail());
                return ResponseEntity.badRequest().body(Map.of("message", "该邮箱尚未注册"));
            }
            
            // 检查用户状态
            if (!user.isActive()) {
                logger.warn("用户账户已被停用: {}", emailLoginRequest.getEmail());
                securityAuditService.logSecurityEvent("ACCOUNT_DISABLED_LOGIN_ATTEMPT", 
                        "尝试使用已停用账户登录", user.getUsername(), ipAddress, "MEDIUM");
                return ResponseEntity.status(403).body(Map.of("message", "账户已被停用，请联系管理员"));
            }
            
            // 生成JWT令牌
            String jwt = jwtUtils.generateJwtTokenForUser(user);
            
            // 记录成功登录
            securityAuditService.logLoginAttempt(user.getUsername(), true, ipAddress, userAgent);
            
            logger.info("邮箱验证码登录成功: {}", user.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, user));
        } catch (Exception e) {
            logger.error("邮箱验证码登录失败: {}", e.getMessage());
            securityAuditService.logLoginAttempt(emailLoginRequest.getEmail(), false, ipAddress, userAgent);
            return ResponseEntity.badRequest().body(Map.of("message", "登录失败: " + e.getMessage()));
        }
    }
    
    // 检查用户名是否已存在
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    // 验证邮箱验证码
    private boolean validateEmailCode(String email, String code) {
        return verificationCodeService.validateVerificationCode(email, code);
    }
    
    // 验证是否是学校学生邮箱
    private boolean isValidStudentEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // 必须是 @student.must.edu.mo 后缀（不区分大小写）
        return email.toLowerCase().endsWith("@student.must.edu.mo");
    }
    
    // 获取客户端IP地址
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // 将英文认证错误信息转换为中文
    private String translateAuthError(String errorMessage) {
        if (errorMessage == null) {
            return "登录失败，请稍后重试";
        }
        
        // Spring Security常见英文错误消息翻译
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("bad credentials")) {
            return "用户名或密码错误";
        }
        if (lowerMessage.contains("user not found") || lowerMessage.contains("username not found")) {
            return "用户不存在";
        }
        if (lowerMessage.contains("account is disabled") || lowerMessage.contains("user is disabled")) {
            return "账户已被禁用";
        }
        if (lowerMessage.contains("account is locked") || lowerMessage.contains("user account is locked")) {
            return "账户已被锁定";
        }
        if (lowerMessage.contains("credentials have expired") || lowerMessage.contains("credentials expired")) {
            return "密码已过期，请重置密码";
        }
        if (lowerMessage.contains("account has expired") || lowerMessage.contains("account expired")) {
            return "账户已过期";
        }
        if (lowerMessage.contains("authentication failed")) {
            return "认证失败，请检查用户名和密码";
        }
        if (lowerMessage.contains("access denied") || lowerMessage.contains("access is denied")) {
            return "访问被拒绝";
        }
        if (lowerMessage.contains("unauthorized")) {
            return "未授权访问";
        }
        if (lowerMessage.contains("forbidden")) {
            return "禁止访问";
        }
        if (lowerMessage.contains("invalid token") || lowerMessage.contains("token")) {
            return "无效的令牌，请重新登录";
        }
        if (lowerMessage.contains("session") && lowerMessage.contains("expired")) {
            return "会话已过期，请重新登录";
        }
        
        // 如果无法匹配，返回原始消息（如果已经是中文）或通用错误
        if (errorMessage.matches(".*[\\u4e00-\\u9fa5].*")) {
            return errorMessage; // 已经是中文
        }
        
        return "登录失败: " + errorMessage;
    }
} 