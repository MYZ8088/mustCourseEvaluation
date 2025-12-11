package com.must.courseevaluation.blackbox;

import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.VerificationCodeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 用户认证模块黑盒测试
 * 
 * 测试功能：用户注册
 * 
 * 测试方法：
 * 1. 等价类划分（Equivalence Partitioning）
 * 2. 边界值测试（Boundary Value Analysis）
 * 3. 因果图（Cause-Effect Graphing）
 * 4. 判定表（Decision Table）
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户认证模块 - 黑盒测试")
public class AuthBlackBoxTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1.1 等价类划分测试 - 用户注册
     */
    @Nested
    @DisplayName("1.1 用户注册 - 等价类划分测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterEquivalencePartitioningTests {

        @Test
        @Order(1)
        @DisplayName("TC_REG_EQ_001: 所有输入有效 - 注册成功")
        @Transactional
        void testRegisterWithAllValidInputs() throws Exception {
            String email = "testeq001@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("user001");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("user001"));
        }

        @Test
        @Order(2)
        @DisplayName("TC_REG_EQ_002: 用户名为空 - 失败")
        void testRegisterWithEmptyUsername() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REG_EQ_003: 用户名太短（2字符）- 失败")
        void testRegisterWithUsernameTooShort() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab");
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REG_EQ_004: 用户名太长（10字符）- 失败")
        void testRegisterWithUsernameTooLong() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(10));
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(5)
        @DisplayName("TC_REG_EQ_005: 用户名已存在 - 失败")
        @Transactional
        void testRegisterWithExistingUsername() throws Exception {
            // 先创建一个用户
            String email = "first@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("existusr");
            firstRequest.setPassword("pass123456");
            firstRequest.setEmail(email);
            firstRequest.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // 尝试使用相同用户名注册
            String email2 = "second@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email2, code);

            RegisterRequest secondRequest = new RegisterRequest();
            secondRequest.setUsername("existusr");
            secondRequest.setPassword("pass123456");
            secondRequest.setEmail(email2);
            secondRequest.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("用户名已被使用")));
        }

        @Test
        @Order(6)
        @DisplayName("TC_REG_EQ_006: 密码为空 - 失败")
        void testRegisterWithEmptyPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        @DisplayName("TC_REG_EQ_007: 密码太短（7字符）- 失败")
        void testRegisterWithPasswordTooShort() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("1234567");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_REG_EQ_008: 密码太长（33字符）- 失败")
        void testRegisterWithPasswordTooLong() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("p".repeat(33));
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(9)
        @DisplayName("TC_REG_EQ_009: 邮箱为空 - 失败")
        void testRegisterWithEmptyEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("pass123456");
            request.setEmail("");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(10)
        @DisplayName("TC_REG_EQ_010: 邮箱格式错误 - 失败")
        void testRegisterWithInvalidEmailFormat() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("pass123456");
            request.setEmail("invalidemail");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(11)
        @DisplayName("TC_REG_EQ_011: 非MUST学生邮箱 - 失败")
        void testRegisterWithNonMustEmail() throws Exception {
            String email = "test@gmail.com";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("澳门科技大学学生邮箱")));
        }

        @Test
        @Order(12)
        @DisplayName("TC_REG_EQ_013: 验证码为空 - 失败")
        void testRegisterWithEmptyVerificationCode() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode(null);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("验证码")));
        }

        @Test
        @Order(13)
        @DisplayName("TC_REG_EQ_014: 验证码错误 - 失败")
        void testRegisterWithWrongVerificationCode() throws Exception {
            String email = "test@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email, "123456");

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode("999999"); // 错误的验证码

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("验证码")));
        }
    }

    /**
     * 1.2 边界值测试 - 用户注册
     */
    @Nested
    @DisplayName("1.2 用户注册 - 边界值测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterBoundaryValueTests {

        @Test
        @Order(1)
        @DisplayName("TC_REG_BV_001: 用户名长度=2 - 失败")
        void testUsernameLength2() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab");
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        @DisplayName("TC_REG_BV_002: 用户名长度=3 - 成功")
        @Transactional
        void testUsernameLength3() throws Exception {
            String email = "testbv002@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("abc");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REG_BV_003: 用户名长度=4 - 成功")
        @Transactional
        void testUsernameLength4() throws Exception {
            String email = "testbv003@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("abcd");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REG_BV_004: 用户名长度=8 - 成功")
        @Transactional
        void testUsernameLength8() throws Exception {
            String email = "testbv004@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(8));
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(5)
        @DisplayName("TC_REG_BV_005: 用户名长度=9 - 成功")
        @Transactional
        void testUsernameLength9() throws Exception {
            String email = "testbv005@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(9));
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(6)
        @DisplayName("TC_REG_BV_006: 用户名长度=10 - 失败")
        void testUsernameLength10() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("a".repeat(10));
            request.setPassword("pass123456");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        @DisplayName("TC_REG_BV_007: 密码长度=7 - 失败")
        void testPasswordLength7() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser1");
            request.setPassword("1234567");
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_REG_BV_008: 密码长度=8 - 成功")
        @Transactional
        void testPasswordLength8() throws Exception {
            String email = "testbv008@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser2");
            request.setPassword("12345678");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(9)
        @DisplayName("TC_REG_BV_009: 密码长度=9 - 成功")
        @Transactional
        void testPasswordLength9() throws Exception {
            String email = "testbv009@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser3");
            request.setPassword("123456789");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(10)
        @DisplayName("TC_REG_BV_010: 密码长度=31 - 成功")
        @Transactional
        void testPasswordLength31() throws Exception {
            String email = "testbv010@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser4");
            request.setPassword("p".repeat(31));
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(11)
        @DisplayName("TC_REG_BV_011: 密码长度=32 - 成功")
        @Transactional
        void testPasswordLength32() throws Exception {
            String email = "testbv011@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser5");
            request.setPassword("p".repeat(32));
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(12)
        @DisplayName("TC_REG_BV_012: 密码长度=33 - 失败")
        void testPasswordLength33() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser6");
            request.setPassword("p".repeat(33));
            request.setEmail("test@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 1.3 因果图测试 - 用户注册
     * 测试各个条件的组合
     */
    @Nested
    @DisplayName("1.3 用户注册 - 因果图测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterCauseEffectTests {

        @Test
        @Order(1)
        @DisplayName("TC_REG_CE_001: C1∧C2∧C3∧C4∧C5 = 1 - 注册成功")
        @Transactional
        void testAllConditionsTrue() throws Exception {
            String email = "testce001@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("validpass123");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("validuser"));
        }

        @Test
        @Order(2)
        @DisplayName("TC_REG_CE_002: C1=0 (用户名格式错误) - 失败")
        void testInvalidUsernameFormat() throws Exception {
            String email = "testce002@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab"); // 太短，格式错误
            request.setPassword("validpass123");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REG_CE_003: C2=0 (用户名已存在) - 失败")
        @Transactional
        void testDuplicateUsername() throws Exception {
            // 先创建第一个用户
            String email1 = "first@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email1, code);

            RegisterRequest firstRequest = new RegisterRequest();
            firstRequest.setUsername("dupuser");
            firstRequest.setPassword("pass123456");
            firstRequest.setEmail(email1);
            firstRequest.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // 尝试创建同名用户
            String email2 = "second@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email2, code);

            RegisterRequest secondRequest = new RegisterRequest();
            secondRequest.setUsername("dupuser");
            secondRequest.setPassword("pass123456");
            secondRequest.setEmail(email2);
            secondRequest.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("用户名已被使用")));
        }

        @Test
        @Order(4)
        @DisplayName("TC_REG_CE_004: C3=0 (密码长度不符) - 失败")
        void testInvalidPasswordLength() throws Exception {
            String email = "testce004@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("1234567"); // 太短（7字符，低于最小8）
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(5)
        @DisplayName("TC_REG_CE_005: C4=0 (非MUST邮箱) - 失败")
        void testNonMustEmail() throws Exception {
            String email = "test@gmail.com";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("validpass123");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("澳门科技大学学生邮箱")));
        }

        @Test
        @Order(6)
        @DisplayName("TC_REG_CE_006: C5=0 (验证码错误) - 失败")
        void testWrongVerificationCode() throws Exception {
            String email = "testce006@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email, "123456");

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("validpass123");
            request.setEmail(email);
            request.setEmailCode("999999"); // 错误验证码

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("验证码")));
        }
    }

    /**
     * 1.4 判定表测试 - 用户注册
     */
    @Nested
    @DisplayName("1.4 用户注册 - 判定表测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterDecisionTableTests {

        @Test
        @Order(1)
        @DisplayName("TC_REG_DT_001: R1 所有条件满足 - 注册成功")
        @Transactional
        void testRule1_AllConditionsMet() throws Exception {
            String email = "testdt001@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("validuser"));
        }

        @Test
        @Order(2)
        @DisplayName("TC_REG_DT_002: R2 用户名格式错误")
        void testRule2_InvalidUsername() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("ab"); // 太短
            request.setPassword("pass123456");
            request.setEmail("testdt002@student.must.edu.mo");
            request.setEmailCode("123456");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REG_DT_004: R4 密码长度错误")
        void testRule4_InvalidPassword() throws Exception {
            String email = "testdt004@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("1234567"); // 太短（7字符，低于最小8）
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REG_DT_005: R5 邮箱格式错误")
        void testRule5_InvalidEmail() throws Exception {
            String email = "test@gmail.com";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode(code);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("澳门科技大学学生邮箱")));
        }

        @Test
        @Order(5)
        @DisplayName("TC_REG_DT_006: R6 验证码错误")
        void testRule6_WrongVerificationCode() throws Exception {
            String email = "testdt006@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email, "123456");

            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setPassword("pass123456");
            request.setEmail(email);
            request.setEmailCode("wrong"); // 错误验证码

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("验证码")));
        }
    }
}

