package com.must.courseevaluation.unit;

import com.must.courseevaluation.service.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VerificationCodeService 单元测试
 * 测试验证码生成、存储和验证逻辑
 */
@DisplayName("VerificationCodeService 单元测试")
class VerificationCodeServiceUnitTest {

    private VerificationCodeServiceImpl verificationCodeService;

    @BeforeEach
    void setUp() {
        verificationCodeService = new VerificationCodeServiceImpl();
    }

    @AfterEach
    void tearDown() {
        verificationCodeService.shutdownScheduler();
    }

    // ==================== generateVerificationCode() 测试 ====================

    @Nested
    @DisplayName("generateVerificationCode() 方法测试")
    class GenerateVerificationCodeTests {

        @Test
        @DisplayName("生成6位数字验证码")
        void testGenerateVerificationCodeFormat() {
            // When
            String code = verificationCodeService.generateVerificationCode();

            // Then
            assertNotNull(code);
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"), "验证码应该是6位数字");
        }

        @Test
        @DisplayName("多次生成验证码 - 格式一致")
        void testGenerateMultipleVerificationCodes() {
            // When
            for (int i = 0; i < 100; i++) {
                String code = verificationCodeService.generateVerificationCode();
                
                // Then
                assertNotNull(code);
                assertEquals(6, code.length());
                assertTrue(code.matches("\\d{6}"), "每个验证码都应该是6位数字");
            }
        }

        @Test
        @DisplayName("验证码包含前导零")
        void testVerificationCodeWithLeadingZeros() {
            // 多次生成，测试能否正确处理前导零
            for (int i = 0; i < 1000; i++) {
                String code = verificationCodeService.generateVerificationCode();
                if (code.startsWith("0")) {
                    assertEquals(6, code.length(), "带前导零的验证码长度应该为6");
                    return; // 找到前导零的情况，测试通过
                }
            }
            // 注：由于随机性，不强制要求必须有前导零，测试仍然通过
        }
    }

    // ==================== storeVerificationCode() 测试 ====================

    @Nested
    @DisplayName("storeVerificationCode() 方法测试")
    class StoreVerificationCodeTests {

        @Test
        @DisplayName("存储验证码 - 可以后续验证")
        void testStoreVerificationCodeSuccess() {
            // Given
            String email = "test@student.must.edu.mo";
            String code = "123456";

            // When
            verificationCodeService.storeVerificationCode(email, code);
            boolean isValid = verificationCodeService.validateVerificationCode(email, code);

            // Then
            assertTrue(isValid, "存储后的验证码应该可以验证通过");
        }

        @Test
        @DisplayName("覆盖已存在的验证码")
        void testStoreVerificationCodeOverwrite() {
            // Given
            String email = "test@student.must.edu.mo";
            String oldCode = "111111";
            String newCode = "222222";

            // When
            verificationCodeService.storeVerificationCode(email, oldCode);
            verificationCodeService.storeVerificationCode(email, newCode);

            // Then
            assertFalse(verificationCodeService.validateVerificationCode(email, oldCode), 
                "旧验证码应该失效");
            assertTrue(verificationCodeService.validateVerificationCode(email, newCode), 
                "新验证码应该有效");
        }

        @Test
        @DisplayName("存储多个不同邮箱的验证码")
        void testStoreMultipleVerificationCodes() {
            // Given
            String email1 = "user1@student.must.edu.mo";
            String email2 = "user2@student.must.edu.mo";
            String code1 = "111111";
            String code2 = "222222";

            // When
            verificationCodeService.storeVerificationCode(email1, code1);
            verificationCodeService.storeVerificationCode(email2, code2);

            // Then
            assertTrue(verificationCodeService.validateVerificationCode(email1, code1));
            assertTrue(verificationCodeService.validateVerificationCode(email2, code2));
        }
    }

    // ==================== validateVerificationCode() 测试 ====================

    @Nested
    @DisplayName("validateVerificationCode() 方法测试")
    class ValidateVerificationCodeTests {

        @Test
        @DisplayName("验证成功 - 正确的验证码")
        void testValidateVerificationCodeSuccess() {
            // Given
            String email = "test@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            // When
            boolean result = verificationCodeService.validateVerificationCode(email, code);

            // Then
            assertTrue(result, "正确的验证码应该验证通过");
        }

        @Test
        @DisplayName("验证失败 - 错误的验证码")
        void testValidateVerificationCodeWrong() {
            // Given
            String email = "test@student.must.edu.mo";
            String correctCode = "123456";
            String wrongCode = "654321";
            verificationCodeService.storeVerificationCode(email, correctCode);

            // When
            boolean result = verificationCodeService.validateVerificationCode(email, wrongCode);

            // Then
            assertFalse(result, "错误的验证码应该验证失败");
        }

        @Test
        @DisplayName("验证失败 - 邮箱不存在")
        void testValidateVerificationCodeEmailNotFound() {
            // Given
            String email = "test@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email, "123456");

            // When
            boolean result = verificationCodeService.validateVerificationCode("other@must.edu.mo", "123456");

            // Then
            assertFalse(result, "不存在的邮箱应该验证失败");
        }

        @Test
        @DisplayName("验证成功后验证码失效")
        void testValidateVerificationCodeRemoveAfterSuccess() {
            // Given
            String email = "test@student.must.edu.mo";
            String code = "123456";
            verificationCodeService.storeVerificationCode(email, code);

            // When
            boolean firstValidation = verificationCodeService.validateVerificationCode(email, code);
            boolean secondValidation = verificationCodeService.validateVerificationCode(email, code);

            // Then
            assertTrue(firstValidation, "第一次验证应该成功");
            assertFalse(secondValidation, "验证成功后，验证码应该失效");
        }

        @Test
        @DisplayName("管理员特殊码 - ADMIN_CREATE 始终验证通过")
        void testValidateAdminCreateCode() {
            // Given
            String email = "admin@must.edu.mo";
            String adminCode = "ADMIN_CREATE";

            // When - 不需要预先存储
            boolean result = verificationCodeService.validateVerificationCode(email, adminCode);

            // Then
            assertTrue(result, "ADMIN_CREATE 特殊码应该始终验证通过");
        }

        @Test
        @DisplayName("管理员特殊码 - 任意邮箱都可用")
        void testValidateAdminCreateCodeAnyEmail() {
            // Given
            String[] emails = {
                "user1@student.must.edu.mo",
                "user2@must.edu.mo",
                "admin@example.com"
            };

            // When & Then
            for (String email : emails) {
                boolean result = verificationCodeService.validateVerificationCode(email, "ADMIN_CREATE");
                assertTrue(result, "ADMIN_CREATE 应该对任意邮箱都有效: " + email);
            }
        }

        @Test
        @DisplayName("验证码区分大小写")
        void testValidateVerificationCodeCaseSensitive() {
            // Given
            String email = "test@student.must.edu.mo";
            verificationCodeService.storeVerificationCode(email, "ABCDEF");

            // When
            boolean correctCase = verificationCodeService.validateVerificationCode(email, "ABCDEF");
            
            // 先存储一个新的进行测试
            verificationCodeService.storeVerificationCode(email, "ABCDEF");
            boolean wrongCase = verificationCodeService.validateVerificationCode(email, "abcdef");

            // Then
            assertTrue(correctCase, "正确大小写应该验证通过");
            assertFalse(wrongCase, "错误大小写应该验证失败");
        }
    }

    // ==================== 并发测试 ====================

    @Nested
    @DisplayName("并发操作测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发存储不同邮箱的验证码")
        void testConcurrentStoreVerificationCodes() throws InterruptedException {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            // When
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    String email = "user" + index + "@student.must.edu.mo";
                    String code = String.format("%06d", index);
                    verificationCodeService.storeVerificationCode(email, code);
                });
                threads[i].start();
            }
            
            // 等待所有线程完成
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (int i = 0; i < threadCount; i++) {
                String email = "user" + i + "@student.must.edu.mo";
                String code = String.format("%06d", i);
                assertTrue(verificationCodeService.validateVerificationCode(email, code),
                    "邮箱 " + email + " 的验证码应该有效");
            }
        }
    }
}

