package com.must.courseevaluation.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户登录功能 - Selenium自动化测试
 * 
 * 测试范围：
 * 1. 登录页面元素验证
 * 2. 账号密码登录功能
 * 3. 登录表单验证（等价类划分、边界值分析）
 * 4. 登录方式切换
 * 5. 登录后跳转验证
 * 
 * 测试数据：使用系统预置的真实账户
 * - 管理员: admin / Password123
 * - 学生: zhangsan / Password123
 * 
 * 测试方法：黑盒测试（等价类划分、边界值分析）
 * 
 * @author Course Evaluation System Team
 * @version 1.0
 */
@DisplayName("登录功能 - Selenium自动化测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginSeleniumTests extends SeleniumTestBase {
    
    // ==================== 页面元素定位器 ====================
    
    /** 用户名输入框 */
    private static final By USERNAME_INPUT = By.id("username");
    
    /** 密码输入框 */
    private static final By PASSWORD_INPUT = By.id("password");
    
    /** 登录按钮 */
    private static final By LOGIN_BUTTON = By.cssSelector("button.btn-primary");
    
    /** 错误提示 */
    private static final By ERROR_ALERT = By.cssSelector(".alert-danger");
    
    /** 成功提示 */
    private static final By SUCCESS_ALERT = By.cssSelector(".alert-success");
    
    /** 注册链接 */
    private static final By REGISTER_LINK = By.cssSelector(".register-link a");
    
    /** 账号密码登录标签 */
    private static final By PASSWORD_TAB = By.xpath("//button[contains(text(), '账号密码登录')]");
    
    /** 邮箱验证码登录标签 */
    private static final By EMAIL_TAB = By.xpath("//button[contains(text(), '邮箱验证码登录')]");
    
    /** 页面标题 */
    private static final By PAGE_TITLE = By.tagName("h1");
    
    // ==================== 页面元素验证测试 ====================
    
    @Test
    @Order(1)
    @DisplayName("TC-LOGIN-001: 验证登录页面元素完整性")
    void testLoginPageElements() {
        navigateTo("/login");
        sleep(1);
        
        // 验证页面标题
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_TITLE));
        assertTrue(title.getText().contains("课程评价系统"), "页面标题应包含'课程评价系统'");
        assertTrue(title.getText().contains("登录"), "页面标题应包含'登录'");
        
        // 验证登录方式切换标签
        assertTrue(driver.findElement(PASSWORD_TAB).isDisplayed(), "账号密码登录标签应可见");
        assertTrue(driver.findElement(EMAIL_TAB).isDisplayed(), "邮箱验证码登录标签应可见");
        
        // 验证用户名输入框
        WebElement usernameInput = driver.findElement(USERNAME_INPUT);
        assertTrue(usernameInput.isDisplayed(), "用户名输入框应可见");
        assertEquals("请输入用户名", usernameInput.getAttribute("placeholder"));
        
        // 验证密码输入框
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        assertTrue(passwordInput.isDisplayed(), "密码输入框应可见");
        assertEquals("请输入密码", passwordInput.getAttribute("placeholder"));
        
        // 验证登录按钮
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        assertTrue(loginBtn.isDisplayed(), "登录按钮应可见");
        assertTrue(loginBtn.getText().contains("登录"));
        
        // 验证注册链接
        WebElement registerLink = driver.findElement(REGISTER_LINK);
        assertTrue(registerLink.isDisplayed(), "注册链接应可见");
        assertEquals("立即注册", registerLink.getText());
    }
    
    // ==================== 等价类划分测试 ====================
    
    @Test
    @Order(2)
    @DisplayName("TC-LOGIN-002: 等价类-有效用户名和密码（学生账户登录）")
    void testValidStudentLogin() {
        navigateTo("/login");
        sleep(1);
        
        // 使用系统预置的学生账户: zhangsan / Password123
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);  // zhangsan
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);  // Password123
        
        // 点击登录
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        assertTrue(loginBtn.isEnabled(), "登录按钮应可点击");
        loginBtn.click();
        sleep(3);
        
        // 验证登录成功：应跳转离开登录页面或显示成功提示
        String currentUrl = getCurrentUrl();
        boolean hasSuccessAlert = driver.findElements(SUCCESS_ALERT).size() > 0;
        boolean pageChanged = !currentUrl.contains("/login");
        
        assertTrue(hasSuccessAlert || pageChanged,
                  "使用有效凭证(zhangsan/Password123)登录应成功");
    }
    
    @Test
    @Order(3)
    @DisplayName("TC-LOGIN-003: 等价类-管理员账户登录")
    void testValidAdminLogin() {
        navigateTo("/login");
        sleep(1);
        
        // 使用系统预置的管理员账户: admin / Password123
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(ADMIN_USERNAME);  // admin
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);  // Password123
        
        driver.findElement(LOGIN_BUTTON).click();
        sleep(3);
        
        // 验证登录成功
        String currentUrl = getCurrentUrl();
        boolean hasSuccessAlert = driver.findElements(SUCCESS_ALERT).size() > 0;
        boolean pageChanged = !currentUrl.contains("/login");
        
        assertTrue(hasSuccessAlert || pageChanged,
                  "使用管理员凭证(admin/Password123)登录应成功");
    }
    
    @Test
    @Order(4)
    @DisplayName("TC-LOGIN-004: 等价类-无效用户名（用户不存在）")
    void testInvalidUsername() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("notexist");  // 不存在的用户（8字符，符合3-9限制）
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        driver.findElement(LOGIN_BUTTON).click();
        sleep(2);
        
        // 验证错误提示出现
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_ALERT));
        assertTrue(error.isDisplayed(), "无效用户名登录应显示错误提示");
        
        // 验证仍停留在登录页面
        assertTrue(getCurrentUrl().contains("/login"), "登录失败应停留在登录页面");
    }
    
    @Test
    @Order(5)
    @DisplayName("TC-LOGIN-005: 等价类-无效密码（密码错误）")
    void testInvalidPassword() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);  // zhangsan - 有效用户名
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys("WrongPassword999");  // 错误密码
        
        driver.findElement(LOGIN_BUTTON).click();
        sleep(2);
        
        // 验证错误提示
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_ALERT));
        assertTrue(error.isDisplayed(), "密码错误时应显示错误提示");
        assertTrue(getCurrentUrl().contains("/login"), "登录失败应停留在登录页面");
    }
    
    @Test
    @Order(6)
    @DisplayName("TC-LOGIN-006: 等价类-空用户名")
    void testEmptyUsername() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();  // 保持为空
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        
        // 检查按钮是否禁用（表单验证）
        assertTrue(loginBtn.getAttribute("disabled") != null || !loginBtn.isEnabled(),
                   "空用户名时登录按钮应被禁用");
    }
    
    @Test
    @Order(7)
    @DisplayName("TC-LOGIN-007: 等价类-空密码")
    void testEmptyPassword() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();  // 保持为空
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        
        // 检查按钮是否禁用
        assertTrue(loginBtn.getAttribute("disabled") != null || !loginBtn.isEnabled(),
                   "空密码时登录按钮应被禁用");
    }
    
    // ==================== 边界值分析测试 ====================
    
    @Test
    @Order(8)
    @DisplayName("TC-LOGIN-008: 边界值-用户名最小长度(3字符)")
    void testUsernameMinLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("abc");  // 3字符，最小有效长度
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        
        // 3字符用户名应该允许提交
        assertFalse(loginBtn.getAttribute("disabled") != null && 
                    loginBtn.getAttribute("disabled").equals("true"),
                    "3字符用户名应允许提交");
    }
    
    @Test
    @Order(9)
    @DisplayName("TC-LOGIN-009: 边界值-用户名低于最小长度(2字符)")
    void testUsernameBelowMinLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("ab");  // 2字符，低于最小长度
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        sleep(1);
        
        // 2字符用户名应该禁用提交按钮
        assertTrue(loginBtn.getAttribute("disabled") != null,
                   "2字符用户名应禁用登录按钮");
    }
    
    @Test
    @Order(10)
    @DisplayName("TC-LOGIN-010: 边界值-用户名最大长度(9字符)")
    void testUsernameMaxLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("user12345");  // 9字符，最大有效长度
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        
        // 9字符用户名应该允许提交
        assertFalse(loginBtn.getAttribute("disabled") != null && 
                    loginBtn.getAttribute("disabled").equals("true"),
                    "9字符用户名应允许提交");
    }
    
    @Test
    @Order(11)
    @DisplayName("TC-LOGIN-011: 边界值-用户名超过最大长度(10字符)")
    void testUsernameAboveMaxLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys("user123456");  // 10字符，超过最大长度
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        sleep(1);
        
        // 10字符用户名应该禁用提交按钮或只保留前9字符
        String actualValue = usernameInput.getAttribute("value");
        assertTrue(actualValue.length() <= 9 || loginBtn.getAttribute("disabled") != null,
                   "超过9字符用户名应被截断或禁用登录按钮");
    }
    
    @Test
    @Order(12)
    @DisplayName("TC-LOGIN-012: 边界值-密码最小长度(8字符)")
    void testPasswordMinLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys("Pass1234");  // 8字符，最小有效长度
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        
        // 8字符密码应该允许提交
        assertFalse(loginBtn.getAttribute("disabled") != null && 
                    loginBtn.getAttribute("disabled").equals("true"),
                    "8字符密码应允许提交");
    }
    
    @Test
    @Order(13)
    @DisplayName("TC-LOGIN-013: 边界值-密码低于最小长度(7字符)")
    void testPasswordBelowMinLength() {
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(STUDENT_USERNAME);
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys("Pass123");  // 7字符，低于最小长度
        
        WebElement loginBtn = driver.findElement(LOGIN_BUTTON);
        sleep(1);
        
        // 7字符密码应该禁用提交按钮
        assertTrue(loginBtn.getAttribute("disabled") != null,
                   "7字符密码应禁用登录按钮");
    }
    
    // ==================== 登录方式切换测试 ====================
    
    @Test
    @Order(14)
    @DisplayName("TC-LOGIN-014: 登录方式切换-账号密码到邮箱验证码")
    void testSwitchToEmailLogin() {
        navigateTo("/login");
        sleep(1);
        
        // 点击邮箱验证码登录标签
        WebElement emailTab = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_TAB));
        emailTab.click();
        sleep(1);
        
        // 验证邮箱输入框出现
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        assertTrue(emailInput.isDisplayed(), "邮箱输入框应可见");
        
        // 验证验证码输入框出现
        WebElement codeInput = driver.findElement(By.id("emailCode"));
        assertTrue(codeInput.isDisplayed(), "验证码输入框应可见");
        
        // 验证获取验证码按钮
        WebElement getCodeBtn = driver.findElement(By.cssSelector(".verification-button"));
        assertTrue(getCodeBtn.isDisplayed(), "获取验证码按钮应可见");
    }
    
    @Test
    @Order(15)
    @DisplayName("TC-LOGIN-015: 登录方式切换-邮箱验证码到账号密码")
    void testSwitchToPasswordLogin() {
        navigateTo("/login");
        sleep(1);
        
        // 先切换到邮箱登录
        WebElement emailTab = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_TAB));
        emailTab.click();
        sleep(1);
        
        // 再切换回密码登录
        WebElement passwordTab = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_TAB));
        passwordTab.click();
        sleep(1);
        
        // 验证用户名和密码输入框出现
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        assertTrue(usernameInput.isDisplayed(), "用户名输入框应可见");
        
        WebElement passwordInput = driver.findElement(PASSWORD_INPUT);
        assertTrue(passwordInput.isDisplayed(), "密码输入框应可见");
    }
    
    // ==================== 链接导航测试 ====================
    
    @Test
    @Order(16)
    @DisplayName("TC-LOGIN-016: 点击注册链接跳转到注册页面")
    void testNavigateToRegister() {
        navigateTo("/login");
        sleep(1);
        
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(REGISTER_LINK));
        registerLink.click();
        sleep(1);
        
        // 验证跳转到注册页面
        assertTrue(getCurrentUrl().contains("/register"), "应跳转到注册页面");
    }
    
    // ==================== 密码显示/隐藏测试 ====================
    
    @Test
    @Order(17)
    @DisplayName("TC-LOGIN-017: 密码显示/隐藏切换功能")
    void testPasswordVisibilityToggle() {
        navigateTo("/login");
        sleep(1);
        
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);
        
        // 默认应该是密码隐藏状态
        assertEquals("password", passwordInput.getAttribute("type"), "默认应为密码隐藏状态");
        
        // 点击显示密码按钮
        WebElement toggleBtn = driver.findElement(By.cssSelector(".password-toggle-btn"));
        toggleBtn.click();
        sleep(1);
        
        // 验证密码变为可见
        assertEquals("text", passwordInput.getAttribute("type"), "点击后应显示密码");
        
        // 再次点击隐藏密码
        toggleBtn.click();
        sleep(1);
        
        // 验证密码变回隐藏
        assertEquals("password", passwordInput.getAttribute("type"), "再次点击后应隐藏密码");
    }
}
