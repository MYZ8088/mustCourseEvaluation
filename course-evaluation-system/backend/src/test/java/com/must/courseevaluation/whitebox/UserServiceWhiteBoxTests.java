package com.must.courseevaluation.whitebox;

import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务模块白盒测试
 * 
 * 测试功能：
 * 1. 用户注册 (register方法)
 * 2. 用户更新 (update方法)
 * 
 * 测试方法：
 * 1. 逻辑覆盖法 (Logical Covering Method)
 *    - 语句覆盖 (Statement Coverage)
 *    - 判定覆盖 (Decision Coverage)
 *    - 条件组合覆盖 (Multiple Condition Coverage)
 * 2. 基本路径法 (Basic Path Method)
 *    - 环路复杂度计算
 *    - 独立路径识别
 *    - 路径覆盖测试
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户服务 - 白盒测试")
public class UserServiceWhiteBoxTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * ==================================================
     * 一、用户注册功能白盒测试
     * 
     * 源代码分析：
     * - 判定D1: userRepository.existsByUsername()
     * - 判定D2: userRepository.existsByEmail()
     * - 环路复杂度: V(G) = 3
     * - 独立路径数: 3
     * ==================================================
     */

    /**
     * 1.1 语句覆盖测试 - 用户注册
     * 目标：覆盖所有可执行语句
     * 覆盖率：100%
     */
    @Nested
    @DisplayName("1.1 用户注册 - 语句覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterStatementCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_UC_ST_001: 所有语句覆盖 - 注册成功")
        @Transactional
        void testAllStatementsCovered() {
            // 准备测试数据 - 新用户名和新邮箱
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser001");
            request.setPassword("password123");
            request.setEmail("newuser001@student.must.edu.mo");
            request.setStudentId("2024001");
            request.setFullName("测试用户001");

            // 执行
            UserDto result = userService.register(request);

            // 验证
            assertNotNull(result);
            assertEquals("newuser001", result.getUsername());
            assertEquals("newuser001@student.must.edu.mo", result.getEmail());
            assertEquals(User.Role.ROLE_STUDENT, result.getRole());

            // 清理
            userRepository.deleteById(result.getId());
        }
    }

    /**
     * 1.2 判定覆盖测试 - 用户注册
     * 目标：覆盖每个判定的真假分支
     * 判定数：2 (D1, D2)
     * 分支数：4
     * 覆盖率：100%
     */
    @Nested
    @DisplayName("1.2 用户注册 - 判定覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterDecisionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_UC_DC_001: D1=True - 用户名已存在")
        @Transactional
        void testD1True_UsernameExists() {
            // 准备 - 先创建一个用户
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("existinguser");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("first@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试 - 尝试使用相同用户名注册
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");  // 相同用户名
            request.setPassword("password123");
            request.setEmail("different@student.must.edu.mo");

            // 验证 - 应该抛出异常
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_UC_DC_002: D1=False, D2=True - 新用户名但邮箱已存在")
        @Transactional
        void testD1False_D2True_EmailExists() {
            // 准备 - 先创建一个用户
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("user001");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("existing@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试 - 新用户名但使用已存在的邮箱
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");  // 不同用户名
            request.setPassword("password123");
            request.setEmail("existing@student.must.edu.mo");  // 相同邮箱

            // 验证 - 应该抛出异常
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("电子邮件已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_UC_DC_003: D1=False, D2=False - 注册成功")
        @Transactional
        void testD1False_D2False_Success() {
            // 准备 - 完全新的用户信息
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser003");
            request.setPassword("password123");
            request.setEmail("newuser003@student.must.edu.mo");

            // 执行
            UserDto result = userService.register(request);

            // 验证
            assertNotNull(result);
            assertEquals("newuser003", result.getUsername());

            // 清理
            userRepository.deleteById(result.getId());
        }
    }

    /**
     * 1.3 条件组合覆盖测试 - 用户注册
     * 目标：覆盖所有条件组合 (2^2 = 4种)
     * 覆盖率：100%
     */
    @Nested
    @DisplayName("1.3 用户注册 - 条件组合覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterMultipleConditionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_UC_CC_001: D1=T, D2=T - 用户名和邮箱都已存在")
        @Transactional
        void testD1True_D2True() {
            // 准备 - 先创建一个用户
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("existuser");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("existemail@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试 - 使用相同用户名和邮箱
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existuser");
            request.setPassword("password123");
            request.setEmail("existemail@student.must.edu.mo");

            // 验证 - 先检查用户名，所以抛出用户名异常
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_UC_CC_002: D1=T, D2=F - 用户名已存在，邮箱未存在")
        @Transactional
        void testD1True_D2False() {
            // 准备
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("existuser2");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("first@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existuser2");  // 已存在
            request.setPassword("password123");
            request.setEmail("newemail@student.must.edu.mo");  // 未存在

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_UC_CC_003: D1=F, D2=T - 用户名未存在，邮箱已存在")
        @Transactional
        void testD1False_D2True() {
            // 准备
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("user003");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("existemail2@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newusername");  // 未存在
            request.setPassword("password123");
            request.setEmail("existemail2@student.must.edu.mo");  // 已存在

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("电子邮件已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_UC_CC_004: D1=F, D2=F - 用户名和邮箱都未存在")
        @Transactional
        void testD1False_D2False() {
            // 准备
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser004");
            request.setPassword("password123");
            request.setEmail("newuser004@student.must.edu.mo");

            // 执行
            UserDto result = userService.register(request);

            // 验证
            assertNotNull(result);
            assertEquals("newuser004", result.getUsername());
            assertEquals("newuser004@student.must.edu.mo", result.getEmail());

            // 清理
            userRepository.deleteById(result.getId());
        }
    }

    /**
     * 1.4 基本路径测试 - 用户注册
     * 环路复杂度：V(G) = 3
     * 独立路径数：3
     * 路径覆盖率：100%
     */
    @Nested
    @DisplayName("1.4 用户注册 - 基本路径测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterBasicPathTests {

        @Test
        @Order(1)
        @DisplayName("TC_UC_BP_001: Path 1 - 用户名已存在路径")
        @Transactional
        void testPath1_UsernameExists() {
            // 准备
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("pathuser1");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("path1@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试 Path 1: START → D1(T) → 异常 → END
            RegisterRequest request = new RegisterRequest();
            request.setUsername("pathuser1");
            request.setPassword("password123");
            request.setEmail("newpath@student.must.edu.mo");

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_UC_BP_002: Path 2 - 用户名不存在但邮箱已存在路径")
        @Transactional
        void testPath2_EmailExists() {
            // 准备
            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("user2");
            firstRequest.setPassword("password123");
            firstRequest.setEmail("path2@student.must.edu.mo");
            UserDto firstUser = userService.register(firstRequest);

            // 测试 Path 2: START → D1(F) → D2(T) → 异常 → END
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser2");  // 新用户名
            request.setPassword("password123");
            request.setEmail("path2@student.must.edu.mo");  // 已存在邮箱

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(request);
            });
            assertTrue(exception.getMessage().contains("电子邮件已被使用"));

            // 清理
            userRepository.deleteById(firstUser.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_UC_BP_003: Path 3 - 成功注册路径")
        @Transactional
        void testPath3_Success() {
            // 准备
            RegisterRequest request = new RegisterRequest();
            request.setUsername("pathuser3");
            request.setPassword("password123");
            request.setEmail("path3@student.must.edu.mo");
            request.setStudentId("2024003");
            request.setFullName("Path Test 3");

            // 测试 Path 3: START → D1(F) → D2(F) → 创建用户 → END
            UserDto result = userService.register(request);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals("pathuser3", result.getUsername());
            assertEquals("path3@student.must.edu.mo", result.getEmail());
            assertEquals("2024003", result.getStudentId());
            assertEquals("Path Test 3", result.getFullName());
            assertEquals(User.Role.ROLE_STUDENT, result.getRole());

            // 清理
            userRepository.deleteById(result.getId());
        }
    }

    /**
     * ==================================================
     * 二、用户更新功能白盒测试
     * 
     * 源代码分析：
     * - 判定D1: username改变
     * - 判定D2: 新username已存在
     * - 判定D3: email改变
     * - 判定D4: 新email已存在
     * - 判定D5: 提供新密码
     * - 环路复杂度: V(G) = 6
     * - 独立路径数: 6
     * ==================================================
     */

    /**
     * 2.1 判定覆盖测试 - 用户更新
     */
    @Nested
    @DisplayName("2.1 用户更新 - 判定覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UserUpdateDecisionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_UU_DC_001: D1=T, D2=T - 更改用户名但已存在")
        @Transactional
        void testD1True_D2True_UsernameExists() {
            // 准备 - 创建两个用户
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("updateuser1");
            req1.setPassword("password123");
            req1.setEmail("update1@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("existingname");
            req2.setPassword("password123");
            req2.setEmail("update2@student.must.edu.mo");
            UserDto user2 = userService.register(req2);

            // 测试 - 尝试将user1的用户名改为user2的用户名
            UserDto updateDto = new UserDto();
            updateDto.setUsername("existingname");  // 已存在的用户名

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.update(user1.getId(), updateDto);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_UU_DC_002: D1=T, D2=F - 更改用户名成功")
        @Transactional
        void testD1True_D2False_UsernameUpdateSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("oldusername");
            req.setPassword("password123");
            req.setEmail("update3@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试 - 更改为新用户名
            UserDto updateDto = new UserDto();
            updateDto.setUsername("newusername");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("newusername", result.getUsername());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_UU_DC_003: D1=F, D3=T, D4=T - 不改用户名，改邮箱但已存在")
        @Transactional
        void testD1False_D3True_D4True_EmailExists() {
            // 准备 - 创建两个用户
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("user1");
            req1.setPassword("password123");
            req1.setEmail("email1@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("user2");
            req2.setPassword("password123");
            req2.setEmail("existingemail@student.must.edu.mo");
            UserDto user2 = userService.register(req2);

            // 测试 - 不改用户名，但改邮箱为已存在的邮箱
            UserDto updateDto = new UserDto();
            updateDto.setEmail("existingemail@student.must.edu.mo");

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.update(user1.getId(), updateDto);
            });
            assertTrue(exception.getMessage().contains("电子邮件已被使用"));

            // 清理
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_UU_DC_004: D1=F, D3=T, D4=F - 更改邮箱成功")
        @Transactional
        void testD3True_D4False_EmailUpdateSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("emailuser");
            req.setPassword("password123");
            req.setEmail("oldemail@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试 - 更改为新邮箱
            UserDto updateDto = new UserDto();
            updateDto.setEmail("newemail@student.must.edu.mo");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("newemail@student.must.edu.mo", result.getEmail());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(5)
        @DisplayName("TC_UU_DC_005: D5=T - 仅更新密码")
        @Transactional
        void testD5True_UpdatePassword() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pwduser");
            req.setPassword("oldpassword");
            req.setEmail("pwd@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试 - 仅更新密码
            UserDto updateDto = new UserDto();
            updateDto.setPassword("newpassword123");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证 - 密码已加密，只能验证更新成功
            assertNotNull(result);
            assertEquals("pwduser", result.getUsername());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(6)
        @DisplayName("TC_UU_DC_006: D1=F, D3=F, D5=F - 不更新敏感字段")
        @Transactional
        void testAllFalse_UpdateOtherFields() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("fielduser");
            req.setPassword("password123");
            req.setEmail("field@student.must.edu.mo");
            req.setStudentId("OLD001");
            UserDto user = userService.register(req);

            // 测试 - 仅更新学号和全名
            UserDto updateDto = new UserDto();
            updateDto.setStudentId("NEW001");
            updateDto.setFullName("新名字");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("NEW001", result.getStudentId());
            assertEquals("新名字", result.getFullName());
            assertEquals("fielduser", result.getUsername());  // 未改变
            assertEquals("field@student.must.edu.mo", result.getEmail());  // 未改变

            // 清理
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 2.2 基本路径测试 - 用户更新
     * 环路复杂度：V(G) = 6
     * 独立路径数：6
     */
    @Nested
    @DisplayName("2.2 用户更新 - 基本路径测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UserUpdateBasicPathTests {

        @Test
        @Order(1)
        @DisplayName("TC_UU_BP_001: Path 1 - 所有字段都不更新")
        @Transactional
        void testPath1_NoSensitiveFieldsUpdate() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("path1user");
            req.setPassword("password123");
            req.setEmail("path1@student.must.edu.mo");
            req.setStudentId("P001");
            UserDto user = userService.register(req);

            // 测试 - 只更新非敏感字段
            UserDto updateDto = new UserDto();
            updateDto.setStudentId("P001-NEW");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("P001-NEW", result.getStudentId());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_UU_BP_002: Path 2 - 仅更新密码")
        @Transactional
        void testPath2_UpdatePasswordOnly() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("path2user");
            req.setPassword("oldpass");
            req.setEmail("path2@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试
            UserDto updateDto = new UserDto();
            updateDto.setPassword("newpassword");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertNotNull(result);

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_UU_BP_003: Path 3 - 更新用户名成功")
        @Transactional
        void testPath3_UpdateUsernameSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("path3old");
            req.setPassword("password123");
            req.setEmail("path3@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试
            UserDto updateDto = new UserDto();
            updateDto.setUsername("path3new");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("path3new", result.getUsername());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_UU_BP_004: Path 4 - 更新用户名失败（已存在）")
        @Transactional
        void testPath4_UpdateUsernameFail() {
            // 准备
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("user4a");
            req1.setPassword("password123");
            req1.setEmail("path4a@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("user4b");
            req2.setPassword("password123");
            req2.setEmail("path4b@student.must.edu.mo");
            UserDto user2 = userService.register(req2);

            // 测试
            UserDto updateDto = new UserDto();
            updateDto.setUsername("user4b");  // 已存在

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.update(user1.getId(), updateDto);
            });
            assertTrue(exception.getMessage().contains("用户名已被使用"));

            // 清理
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }

        @Test
        @Order(5)
        @DisplayName("TC_UU_BP_005: Path 5 - 更新邮箱成功")
        @Transactional
        void testPath5_UpdateEmailSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("path5user");
            req.setPassword("password123");
            req.setEmail("path5old@student.must.edu.mo");
            UserDto user = userService.register(req);

            // 测试
            UserDto updateDto = new UserDto();
            updateDto.setEmail("path5new@student.must.edu.mo");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            // 执行
            UserDto result = userService.update(user.getId(), updateDto);

            // 验证
            assertEquals("path5new@student.must.edu.mo", result.getEmail());

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(6)
        @DisplayName("TC_UU_BP_006: Path 6 - 更新邮箱失败（已存在）")
        @Transactional
        void testPath6_UpdateEmailFail() {
            // 准备
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("user6a");
            req1.setPassword("password123");
            req1.setEmail("path6a@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("user6b");
            req2.setPassword("password123");
            req2.setEmail("path6b@student.must.edu.mo");
            UserDto user2 = userService.register(req2);

            // 测试
            UserDto updateDto = new UserDto();
            updateDto.setEmail("path6b@student.must.edu.mo");  // 已存在

            // 验证
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.update(user1.getId(), updateDto);
            });
            assertTrue(exception.getMessage().contains("电子邮件已被使用"));

            // 清理
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }
    }
}


