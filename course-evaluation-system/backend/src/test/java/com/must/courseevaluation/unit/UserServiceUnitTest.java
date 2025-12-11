package com.must.courseevaluation.unit;

import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 使用 Mockito 模拟依赖，测试 UserServiceImpl 的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@student.must.edu.mo");
        testUser.setStudentId("2024001");
        testUser.setFullName("测试用户");
        testUser.setRole(User.Role.ROLE_STUDENT);
        testUser.setActive(true);
        testUser.setCanComment(true);

        // 初始化注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@student.must.edu.mo");
        registerRequest.setStudentId("2024002");
        registerRequest.setFullName("新用户");
    }

    // ==================== register() 测试 ====================

    @Nested
    @DisplayName("register() 方法测试")
    class RegisterTests {

        @Test
        @DisplayName("成功注册新用户")
        void testRegisterSuccess() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@student.must.edu.mo")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return user;
            });

            // When
            UserDto result = userService.register(registerRequest);

            // Then
            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("newuser@student.must.edu.mo", result.getEmail());
            assertEquals(User.Role.ROLE_STUDENT, result.getRole());
            
            verify(userRepository).existsByUsername("newuser");
            verify(userRepository).existsByEmail("newuser@student.must.edu.mo");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("用户名已存在 - 抛出异常")
        void testRegisterUsernameExists() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.register(registerRequest));
            
            assertEquals("用户名已被使用", exception.getMessage());
            verify(userRepository).existsByUsername("newuser");
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("邮箱已存在 - 抛出异常")
        void testRegisterEmailExists() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@student.must.edu.mo")).thenReturn(true);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.register(registerRequest));
            
            assertEquals("电子邮件已被使用", exception.getMessage());
            verify(userRepository).existsByUsername("newuser");
            verify(userRepository).existsByEmail("newuser@student.must.edu.mo");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ==================== findByUsername() 测试 ====================

    @Nested
    @DisplayName("findByUsername() 方法测试")
    class FindByUsernameTests {

        @Test
        @DisplayName("成功查找用户")
        void testFindByUsernameSuccess() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When
            User result = userService.findByUsername("testuser");

            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals(1L, result.getId());
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        @DisplayName("用户不存在 - 抛出异常")
        void testFindByUsernameNotFound() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, 
                () -> userService.findByUsername("nonexistent"));
            verify(userRepository).findByUsername("nonexistent");
        }
    }

    // ==================== findById() 测试 ====================

    @Nested
    @DisplayName("findById() 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("成功查找用户")
        void testFindByIdSuccess() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            User result = userService.findById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("用户不存在 - 抛出异常")
        void testFindByIdNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.findById(999L));
            
            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userRepository).findById(999L);
        }
    }

    // ==================== findByEmail() 测试 ====================

    @Nested
    @DisplayName("findByEmail() 方法测试")
    class FindByEmailTests {

        @Test
        @DisplayName("成功查找用户")
        void testFindByEmailSuccess() {
            // Given
            when(userRepository.findByEmail("test@student.must.edu.mo")).thenReturn(Optional.of(testUser));

            // When
            User result = userService.findByEmail("test@student.must.edu.mo");

            // Then
            assertNotNull(result);
            assertEquals("test@student.must.edu.mo", result.getEmail());
            verify(userRepository).findByEmail("test@student.must.edu.mo");
        }

        @Test
        @DisplayName("邮箱不存在 - 返回null")
        void testFindByEmailNotFound() {
            // Given
            when(userRepository.findByEmail("nonexistent@must.edu.mo")).thenReturn(Optional.empty());

            // When
            User result = userService.findByEmail("nonexistent@must.edu.mo");

            // Then
            assertNull(result);
            verify(userRepository).findByEmail("nonexistent@must.edu.mo");
        }
    }

    // ==================== findAll() 测试 ====================

    @Nested
    @DisplayName("findAll() 方法测试")
    class FindAllTests {

        @Test
        @DisplayName("返回用户列表")
        void testFindAllSuccess() {
            // Given
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");
            user2.setEmail("user2@student.must.edu.mo");
            user2.setRole(User.Role.ROLE_STUDENT);
            
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

            // When
            List<UserDto> result = userService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("testuser", result.get(0).getUsername());
            assertEquals("user2", result.get(1).getUsername());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("无用户 - 返回空列表")
        void testFindAllEmpty() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<UserDto> result = userService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }

    // ==================== update() 测试 ====================

    @Nested
    @DisplayName("update() 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("成功更新用户信息")
        void testUpdateSuccess() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("testuser"); // 保持不变
            updateDto.setEmail("test@student.must.edu.mo"); // 保持不变
            updateDto.setFullName("更新后的名字");
            updateDto.setStudentId("2024001");
            updateDto.setActive(true);
            updateDto.setCanComment(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.update(1L, updateDto);

            // Then
            assertNotNull(result);
            verify(userRepository).findById(1L);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("更新用户名 - 新用户名不冲突")
        void testUpdateUsernameSuccess() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("newusername");
            updateDto.setEmail("test@student.must.edu.mo");
            updateDto.setActive(true);
            updateDto.setCanComment(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("newusername")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.update(1L, updateDto);

            // Then
            assertNotNull(result);
            verify(userRepository).existsByUsername("newusername");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("更新用户名 - 用户名已存在")
        void testUpdateUsernameConflict() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("existinguser");
            updateDto.setEmail("test@student.must.edu.mo");
            updateDto.setActive(true);
            updateDto.setCanComment(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.update(1L, updateDto));
            
            assertEquals("用户名已被使用", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("更新邮箱 - 邮箱已存在")
        void testUpdateEmailConflict() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("testuser");
            updateDto.setEmail("existing@student.must.edu.mo");
            updateDto.setActive(true);
            updateDto.setCanComment(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("existing@student.must.edu.mo")).thenReturn(true);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.update(1L, updateDto));
            
            assertEquals("电子邮件已被使用", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("更新密码")
        void testUpdatePassword() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("testuser");
            updateDto.setEmail("test@student.must.edu.mo");
            updateDto.setPassword("newpassword");
            updateDto.setActive(true);
            updateDto.setCanComment(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.update(1L, updateDto);

            // Then
            assertNotNull(result);
            verify(passwordEncoder).encode("newpassword");
            verify(userRepository).save(any(User.class));
        }
    }

    // ==================== delete() 测试 ====================

    @Nested
    @DisplayName("delete() 方法测试")
    class DeleteTests {

        @Test
        @DisplayName("成功删除用户")
        void testDeleteSuccess() {
            // Given
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            // When
            userService.delete(1L);

            // Then
            verify(userRepository).existsById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("用户不存在 - 抛出异常")
        void testDeleteNotFound() {
            // Given
            when(userRepository.existsById(999L)).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.delete(999L));
            
            assertTrue(exception.getMessage().contains("用户不存在"));
            verify(userRepository).existsById(999L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    // ==================== existsByUsername() / existsByEmail() 测试 ====================

    @Nested
    @DisplayName("exists 方法测试")
    class ExistsTests {

        @Test
        @DisplayName("existsByUsername - 用户名存在")
        void testExistsByUsernameTrue() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // When
            boolean result = userService.existsByUsername("testuser");

            // Then
            assertTrue(result);
            verify(userRepository).existsByUsername("testuser");
        }

        @Test
        @DisplayName("existsByUsername - 用户名不存在")
        void testExistsByUsernameFalse() {
            // Given
            when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

            // When
            boolean result = userService.existsByUsername("nonexistent");

            // Then
            assertFalse(result);
            verify(userRepository).existsByUsername("nonexistent");
        }

        @Test
        @DisplayName("existsByEmail - 邮箱存在")
        void testExistsByEmailTrue() {
            // Given
            when(userRepository.existsByEmail("test@student.must.edu.mo")).thenReturn(true);

            // When
            boolean result = userService.existsByEmail("test@student.must.edu.mo");

            // Then
            assertTrue(result);
            verify(userRepository).existsByEmail("test@student.must.edu.mo");
        }

        @Test
        @DisplayName("existsByEmail - 邮箱不存在")
        void testExistsByEmailFalse() {
            // Given
            when(userRepository.existsByEmail("nonexistent@must.edu.mo")).thenReturn(false);

            // When
            boolean result = userService.existsByEmail("nonexistent@must.edu.mo");

            // Then
            assertFalse(result);
            verify(userRepository).existsByEmail("nonexistent@must.edu.mo");
        }
    }
}




