package com.must.courseevaluation.whitebox;

import com.must.courseevaluation.controller.AuthController;
import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.EmailService;
import com.must.courseevaluation.service.SecurityAuditService;
import com.must.courseevaluation.service.UserService;
import com.must.courseevaluation.service.VerificationCodeService;
import com.must.courseevaluation.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhiteBoxAuthTest {

    @Nested
    @DisplayName("Basic Path Testing - UserServiceImpl.register")
    class UserServiceBasicPathTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UserServiceImpl userService;

        private RegisterRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new RegisterRequest();
            validRequest.setUsername("testuser");
            validRequest.setPassword("password");
            validRequest.setEmail("test@example.com");
            validRequest.setStudentId("S123456");
            validRequest.setFullName("Test User");
        }

        @Test
        @DisplayName("Path 1: Username already exists")
        void testRegister_Path1_UsernameExists() {
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(validRequest);
            });
            assertEquals("用户名已被使用", exception.getMessage());
            
            verify(userRepository).existsByUsername(validRequest.getUsername());
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Path 2: Email already exists")
        void testRegister_Path2_EmailExists() {
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(validRequest);
            });
            assertEquals("电子邮件已被使用", exception.getMessage());

            verify(userRepository).existsByUsername(validRequest.getUsername());
            verify(userRepository).existsByEmail(validRequest.getEmail());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Path 3: Successful Registration")
        void testRegister_Path3_Success() {
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encodedPassword");
            
            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername(validRequest.getUsername());
            savedUser.setEmail(validRequest.getEmail());
            savedUser.setRole(User.Role.ROLE_STUDENT);
            
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserDto result = userService.register(validRequest);

            assertNotNull(result);
            assertEquals(validRequest.getUsername(), result.getUsername());
            
            verify(userRepository).existsByUsername(validRequest.getUsername());
            verify(userRepository).existsByEmail(validRequest.getEmail());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Logical Covering Testing - AuthController.registerUser")
    class AuthControllerLogicalCoveringTest {

        @Mock
        private UserService userService;

        @Mock
        private VerificationCodeService verificationCodeService;
        
        @Mock
        private EmailService emailService;
        
        @Mock
        private SecurityAuditService securityAuditService;

        @InjectMocks
        private AuthController authController;

        private RegisterRequest request;

        @BeforeEach
        void setUp() {
            request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@student.must.edu.mo");
            request.setPassword("password123");
            request.setEmailCode("123456");
        }

        @Test
        @DisplayName("Branch 1: Username exists")
        void testRegisterUser_UsernameExists() {
            when(userService.existsByUsername(request.getUsername())).thenReturn(true);

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            assertEquals("错误: 用户名已被使用!", body.get("message"));
        }

        @Test
        @DisplayName("Branch 2: Email exists")
        void testRegisterUser_EmailExists() {
            when(userService.existsByUsername(request.getUsername())).thenReturn(false);
            when(userService.existsByEmail(request.getEmail())).thenReturn(true);

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            assertEquals("错误: 电子邮件已被使用!", body.get("message"));
        }

        @Test
        @DisplayName("Branch 3: Invalid Email Code")
        void testRegisterUser_InvalidEmailCode() {
            when(userService.existsByUsername(request.getUsername())).thenReturn(false);
            when(userService.existsByEmail(request.getEmail())).thenReturn(false);
            when(verificationCodeService.validateVerificationCode(request.getEmail(), request.getEmailCode())).thenReturn(false);

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            assertEquals("错误: 邮箱验证码无效或已过期!", body.get("message"));
        }

        @Test
        @DisplayName("Branch 4: Registration Exception")
        void testRegisterUser_RegistrationException() {
            when(userService.existsByUsername(request.getUsername())).thenReturn(false);
            when(userService.existsByEmail(request.getEmail())).thenReturn(false);
            when(verificationCodeService.validateVerificationCode(request.getEmail(), request.getEmailCode())).thenReturn(true);
            
            when(userService.register(any())).thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("用户创建失败"));
        }

        @Test
        @DisplayName("Branch 5: Success")
        void testRegisterUser_Success() {
            when(userService.existsByUsername(request.getUsername())).thenReturn(false);
            when(userService.existsByEmail(request.getEmail())).thenReturn(false);
            when(verificationCodeService.validateVerificationCode(request.getEmail(), request.getEmailCode())).thenReturn(true);
            
            UserDto userDto = new UserDto();
            userDto.setUsername(request.getUsername());
            when(userService.register(any())).thenReturn(userDto);

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(userDto, response.getBody());
        }

        @Test
        @DisplayName("Condition: Admin Create Bypass")
        void testRegisterUser_AdminCreate() {
            request.setEmailCode("ADMIN_CREATE");
            
            when(userService.existsByUsername(request.getUsername())).thenReturn(false);
            when(userService.existsByEmail(request.getEmail())).thenReturn(false);
            
            UserDto userDto = new UserDto();
            userDto.setUsername(request.getUsername());
            when(userService.register(any())).thenReturn(userDto);

            ResponseEntity<?> response = authController.registerUser(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(verificationCodeService, never()).validateVerificationCode(anyString(), anyString());
        }
    }
}

