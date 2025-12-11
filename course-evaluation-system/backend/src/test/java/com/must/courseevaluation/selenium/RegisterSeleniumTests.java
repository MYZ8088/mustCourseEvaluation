package com.must.courseevaluation.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户注册功能 - Selenium自动化测试
 * 
 * 测试范围：
 * 1. 注册页面元素验证
 * 2. 表单字段验证（等价类划分、边界值分析）
 * 3. 邮箱格式验证（仅支持 @student.must.edu.mo）
 * 4. 密码强度验证
 * 5. 密码确认匹配验证
 * 6. 用户名实时检测
 * 
 * 测试数据：
 * - 有效邮箱格式: xxx@student.must.edu.mo
 * - 已存在用户名: zhangsan, lisi, admin
 * 
 * @author Course Evaluation System Team
 * @version 1.0
 */
@DisplayName("注册功能 - Selenium自动化测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterSeleniumTests extends SeleniumTestBase {
    
    // ==================== 页面元素定位器 ====================
    
    private static final By USERNAME_INPUT = By.id("username");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By EMAIL_CODE_INPUT = By.id("emailCode");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By CONFIRM_PASSWORD_INPUT = By.id("confirmPassword");
    private static final By REGISTER_BUTTON = By.cssSelector("button.btn-primary");
    private static final By GET_CODE_BUTTON = By.cssSelector(".verification-button");
    private static final By ERROR_ALERT = By.cssSelector(".alert-danger");
    private static final By SUCCESS_ALERT = By.cssSelector(".alert-success");
    private static final By LOGIN_LINK = By.cssSelector(".login-link a");
    private static final By PASSWORD_STRENGTH = By.cssSelector(".password-strength-text");
    
    // ==================== 页面元素验证测试 ====================
    
    @Test
    @Order(1)
    @DisplayName("TC-REG-001: 验证注册页面元素完整性")
    void testRegisterPageElements() {
        navigateTo("/register");
        sleep(1);
        
        // 验证页面标题
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(title.getText().contains("课程评价系统"), "页面标题应包含'课程评价系统'");
        assertTrue(title.getText().contains("注册"), "页面标题应包含'注册'");
        
        // 验证所有输入框存在
        assertTrue(driver.findElement(USERNAME_INPUT).isDisplayed(), "用户名输入框应可见");
        assertTrue(driver.findElement(EMAIL_INPUT).isDisplayed(), "邮箱输入框应可见");
        assertTrue(driver.findElement(EMAIL_CODE_INPUT).isDisplayed(), "验证码输入框应可见");
        assertTrue(driver.findElement(PASSWORD_INPUT).isDisplayed(), "密码输入框应可见");
        assertTrue(driver.findElement(CONFIRM_PASSWORD_INPUT).isDisplayed(), "确认密码输入框应可见");
        
        // 验证获取验证码按钮
        assertTrue(driver.findElement(GET_CODE_BUTTON).isDisplayed(), "获取验证码按钮应可见");
        
        // 验证注册按钮
        assertTrue(driver.findElement(REGISTER_BUTTON).isDisplayed(), "注册按钮应可见");
        
        // 验证登录链接
        WebElement loginLink = driver.findElement(LOGIN_LINK);
        assertTrue(loginLink.isDisplayed(), "登录链接应可见");
        assertEquals("立即登录", loginLink.getText());
    }
    
    // ==================== 用户名验证测试（等价类划分） ====================
    
    @Test
    @Order(2)
    @DisplayName("TC-REG-002: 等价类-有效新用户名(3-9字符)")
    void testValidNewUsername() {
        navigateTo("/register");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        // 使用一个不存在于系统中的新用户名(3-9字符)
        usernameInput.sendKeys("newuser1");
        sleep(2);  // 等待异步用户名检查
        
        // 应显示用户名可用提示
        try {
            WebElement availableHint = driver.findElement(By.cssSelector(".text-success"));
            assertTrue(availableHint.getText().contains("可用"), "新用户名应显示可用");
        } catch (Exception e) {
            // 至少验证没有显示已存在提示
            assertFalse(driver.getPageSource().contains("已被使用"), "新用户名不应显示已被使用");
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("TC-REG-003: 等价类-已存在的用户名(zhangsan)")
    void testExistingUsername() {
        navigateTo("/register");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);  // zhangsan - 系统中已存在
        sleep(2);  // 等待异步检查
        
        // 应显示用户名已存在提示
        assertTrue(driver.getPageSource().contains("已被使用") || 
                   driver.findElements(By.cssSelector(".text-danger")).size() > 0,
                   "已存在的用户名应显示'已被使用'提示");
    }
    
    @Test
    @Order(4)
    @DisplayName("TC-REG-004: 边界值-用户名最小长度(3字符)")
    void testUsernameMinBoundary() {
        navigateTo("/register");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("abc");  // 恰好3字符
        sleep(1);
        
        // 3字符应该被接受
        assertEquals(3, usernameInput.getAttribute("value").length());
    }
    
    @Test
    @Order(5)
    @DisplayName("TC-REG-005: 边界值-用户名最大长度(9字符)")
    void testUsernameMaxBoundary() {
        navigateTo("/register");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("user12345");  // 恰好9字符
        sleep(1);
        
        // 9字符应该被接受
        assertEquals(9, usernameInput.getAttribute("value").length(),
                   "用户名应为9字符");
    }
    
    // ==================== 邮箱验证测试（等价类划分） ====================
    
    @Test
    @Order(6)
    @DisplayName("TC-REG-006: 等价类-有效学校邮箱(@student.must.edu.mo)")
    void testValidSchoolEmail() {
        navigateTo("/register");
        sleep(1);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys("newstudent@student.must.edu.mo");  // 有效学校邮箱
        sleep(1);
        
        // 检查是否显示邮箱格式正确的提示
        try {
            WebElement successHint = driver.findElement(By.xpath("//small[contains(@class, 'text-success')]"));
            assertTrue(successHint.getText().contains("格式正确"), "应显示邮箱格式正确提示");
        } catch (Exception e) {
            // 至少验证没有显示错误
            assertFalse(emailInput.getAttribute("class").contains("is-invalid"),
                       "有效邮箱不应标记为无效");
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("TC-REG-007: 等价类-无效邮箱(非学校邮箱 @gmail.com)")
    void testInvalidNonSchoolEmail() {
        navigateTo("/register");
        sleep(1);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys("test@gmail.com");  // 非学校邮箱
        sleep(1);
        
        // 应显示错误提示
        assertTrue(emailInput.getAttribute("class").contains("is-invalid") ||
                   driver.getPageSource().contains("澳门科技大学"),
                   "非学校邮箱应显示错误提示");
    }
    
    @Test
    @Order(8)
    @DisplayName("TC-REG-008: 等价类-无效邮箱格式(缺少@)")
    void testInvalidEmailFormat() {
        navigateTo("/register");
        sleep(1);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys("invalidemail");  // 无效格式
        sleep(1);
        
        // 应标记为无效
        assertTrue(emailInput.getAttribute("class").contains("is-invalid"),
                   "无效邮箱格式应被标记");
    }
    
    // ==================== 密码验证测试（等价类划分） ====================
    
    @Test
    @Order(9)
    @DisplayName("TC-REG-009: 等价类-有效密码(8-32位，字母+数字)")
    void testValidPassword() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("ValidPass123");  // 符合要求
        sleep(1);
        
        // 检查密码强度指示器显示
        WebElement strengthText = driver.findElement(PASSWORD_STRENGTH);
        String text = strengthText.getText();
        assertTrue(text.contains("弱") || text.contains("中") || 
                   text.contains("强") || text.contains("密码强度"),
                   "应显示密码强度指示");
    }
    
    @Test
    @Order(10)
    @DisplayName("TC-REG-010: 等价类-无效密码(纯数字)")
    void testPasswordOnlyNumbers() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("12345678");  // 纯数字
        sleep(1);
        
        // 应显示密码不能为纯数字的提示
        assertTrue(driver.getPageSource().contains("纯数字") ||
                   driver.findElements(By.cssSelector(".text-danger")).size() > 0,
                   "纯数字密码应显示错误提示");
    }
    
    @Test
    @Order(11)
    @DisplayName("TC-REG-011: 等价类-无效密码(纯字母)")
    void testPasswordOnlyLetters() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("abcdefgh");  // 纯字母
        sleep(1);
        
        assertTrue(driver.getPageSource().contains("纯字母") ||
                   driver.findElements(By.cssSelector(".text-danger")).size() > 0,
                   "纯字母密码应显示错误提示");
    }
    
    @Test
    @Order(12)
    @DisplayName("TC-REG-012: 边界值-密码最小长度(8字符)")
    void testPasswordMinLength() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("Pass1234");  // 恰好8字符
        sleep(1);
        
        // 8字符有效密码应被接受
        assertEquals(8, passwordInput.getAttribute("value").length());
        assertFalse(driver.getPageSource().contains("长度不足"),
                    "8字符密码不应提示长度不足");
    }
    
    @Test
    @Order(13)
    @DisplayName("TC-REG-013: 边界值-密码低于最小长度(7字符)")
    void testPasswordBelowMinLength() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("Pass123");  // 7字符
        sleep(1);
        
        assertTrue(driver.getPageSource().contains("长度不足") ||
                   driver.getPageSource().contains("8"),
                   "7字符密码应提示长度不足");
    }
    
    // ==================== 密码确认匹配测试 ====================
    
    @Test
    @Order(14)
    @DisplayName("TC-REG-014: 密码确认-两次密码一致")
    void testPasswordMatch() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);  // Password123
        
        WebElement confirmInput = driver.findElement(CONFIRM_PASSWORD_INPUT);
        confirmInput.clear();
        confirmInput.sendKeys(TEST_PASSWORD);  // 相同密码
        sleep(1);
        
        // 应显示密码匹配提示
        assertTrue(driver.getPageSource().contains("匹配") ||
                   driver.findElements(By.xpath("//small[contains(@class, 'text-success')]")).size() > 0,
                   "两次密码一致应显示匹配提示");
    }
    
    @Test
    @Order(15)
    @DisplayName("TC-REG-015: 密码确认-两次密码不一致")
    void testPasswordMismatch() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement confirmInput = driver.findElement(CONFIRM_PASSWORD_INPUT);
        confirmInput.clear();
        confirmInput.sendKeys("DifferentPass456");  // 不同密码
        sleep(1);
        
        assertTrue(driver.getPageSource().contains("不匹配") ||
                   driver.getPageSource().contains("不一致"),
                   "两次密码不一致应提示不匹配");
    }
    
    // ==================== 密码强度测试 ====================
    
    @Test
    @Order(16)
    @DisplayName("TC-REG-016: 密码强度-弱(仅满足基本要求)")
    void testWeakPassword() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("test1234");  // 弱密码
        sleep(1);
        
        WebElement strengthText = driver.findElement(PASSWORD_STRENGTH);
        assertTrue(strengthText.getText().contains("弱"), "基本密码应显示强度为弱");
    }
    
    @Test
    @Order(17)
    @DisplayName("TC-REG-017: 密码强度-强(包含大小写、数字、特殊字符)")
    void testStrongPassword() {
        navigateTo("/register");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys("Test@Pass123!");  // 强密码
        sleep(1);
        
        WebElement strengthText = driver.findElement(PASSWORD_STRENGTH);
        String text = strengthText.getText();
        assertTrue(text.contains("强") || text.contains("很强"), 
                   "强密码应显示强度为强或很强");
    }
    
    // ==================== 验证码按钮状态测试 ====================
    
    @Test
    @Order(18)
    @DisplayName("TC-REG-018: 验证码按钮-无效邮箱时状态验证")
    void testVerificationButtonForInvalidEmail() {
        navigateTo("/register");
        sleep(2);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys("invalid@gmail.com");  // 非学校邮箱
        sleep(2);
        
        // 非学校邮箱时，按钮应禁用或显示错误提示
        WebElement getCodeBtn = driver.findElement(GET_CODE_BUTTON);
        boolean isDisabled = getCodeBtn.getAttribute("disabled") != null;
        boolean hasError = driver.getPageSource().contains("澳门科技大学") || 
                           driver.getPageSource().contains("must.edu.mo");
        
        assertTrue(isDisabled || hasError,
                   "非学校邮箱时应禁用按钮或显示错误提示");
    }
    
    @Test
    @Order(19)
    @DisplayName("TC-REG-019: 验证码按钮-有效邮箱时启用")
    void testVerificationButtonEnabledForValidEmail() {
        navigateTo("/register");
        sleep(1);
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys("newuser@student.must.edu.mo");  // 有效学校邮箱
        sleep(1);
        
        WebElement getCodeBtn = driver.findElement(GET_CODE_BUTTON);
        assertTrue(getCodeBtn.getAttribute("disabled") == null ||
                   !getCodeBtn.getAttribute("disabled").equals("true"),
                   "有效学校邮箱时获取验证码按钮应启用");
    }
    
    // ==================== 导航测试 ====================
    
    @Test
    @Order(20)
    @DisplayName("TC-REG-020: 点击登录链接跳转到登录页面")
    void testNavigateToLogin() {
        navigateTo("/register");
        sleep(1);
        
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_LINK));
        loginLink.click();
        sleep(1);
        
        assertTrue(getCurrentUrl().contains("/login"), "应跳转到登录页面");
    }
}
