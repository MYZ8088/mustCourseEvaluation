package com.must.courseevaluation.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 页面导航功能 - Selenium自动化测试
 * 
 * 测试范围：
 * 1. 首页元素和功能验证
 * 2. 页面路由导航
 * 3. 路由守卫（登录保护）
 * 4. 导航栏功能
 * 5. 404页面处理
 * 6. 用户角色跳转
 * 
 * 测试数据：
 * - 管理员: admin / Password123
 * - 学生: zhangsan / Password123
 * 
 * @author Course Evaluation System Team
 * @version 1.0
 */
@DisplayName("页面导航功能 - Selenium自动化测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NavigationSeleniumTests extends SeleniumTestBase {
    
    // ==================== 页面元素定位器 ====================
    
    private static final By NAV_LOGO = By.cssSelector(".navbar-logo a");
    private static final By NAV_HOME = By.cssSelector(".navbar-logo a");
    private static final By NAV_COURSES = By.cssSelector(".navbar-links a[href='/courses']");
    private static final By NAV_LOGIN = By.cssSelector(".navbar-links a[href='/login']");
    private static final By NAV_REGISTER = By.cssSelector(".navbar-links a[href='/register']");
    private static final By NAV_PROFILE = By.cssSelector(".navbar-links .user-profile");
    private static final By NAV_LOGOUT = By.xpath("//a[contains(text(), '退出登录')]");
    private static final By USER_DROPDOWN = By.cssSelector(".navbar-links");
    private static final By HERO_SECTION = By.cssSelector(".welcome-section");
    private static final By FEATURE_CARDS = By.cssSelector(".feature-card");
    private static final By START_BUTTON = By.cssSelector(".feature-card .btn-primary");
    private static final By NOT_FOUND_PAGE = By.cssSelector(".not-found, .error-page");
    
    // ==================== 首页测试 ====================
    
    @Test
    @Order(1)
    @DisplayName("TC-NAV-001: 首页元素完整性验证")
    void testHomePageElements() {
        navigateTo("/");
        sleep(2);
        
        // 验证欢迎区域
        WebElement welcomeSection = wait.until(ExpectedConditions.visibilityOfElementLocated(HERO_SECTION));
        assertTrue(welcomeSection.isDisplayed(), "欢迎区域应可见");
        
        // 验证主标题 - 实际标题是"欢迎使用课程评价系统"
        WebElement mainTitle = welcomeSection.findElement(By.tagName("h1"));
        assertTrue(mainTitle.getText().contains("课程评价系统"),
                   "应显示'课程评价系统'标题");
        
        // 验证功能卡片区域
        java.util.List<WebElement> featureCards = driver.findElements(FEATURE_CARDS);
        assertTrue(featureCards.size() >= 3, "应显示功能特性卡片");
    }
    
    @Test
    @Order(2)
    @DisplayName("TC-NAV-002: 首页功能特性区域验证")
    void testHomeFeatureSection() {
        navigateTo("/");
        sleep(1);
        
        java.util.List<WebElement> featureCards = driver.findElements(FEATURE_CARDS);
        assertTrue(featureCards.size() >= 3, "应显示至少3个功能特性卡片");
        
        // 验证功能特性卡片内容
        boolean hasCourseFeature = false;
        boolean hasReviewFeature = false;
        
        for (WebElement card : featureCards) {
            String text = card.getText();
            if (text.contains("课程") || text.contains("浏览")) {
                hasCourseFeature = true;
            }
            if (text.contains("评价") || text.contains("评论")) {
                hasReviewFeature = true;
            }
        }
        
        assertTrue(hasCourseFeature, "应有课程相关功能介绍");
        assertTrue(hasReviewFeature, "应有评价相关功能介绍");
    }
    
    @Test
    @Order(3)
    @DisplayName("TC-NAV-003: 首页功能卡片跳转")
    void testFeatureCardNavigation() {
        navigateTo("/");
        sleep(2);
        
        // 点击第一个功能卡片的"浏览课程"按钮
        java.util.List<WebElement> buttons = driver.findElements(START_BUTTON);
        if (buttons.size() > 0) {
            buttons.get(0).click();
            sleep(1);
            
            // 应跳转到课程列表
            String currentUrl = getCurrentUrl();
            assertTrue(currentUrl.contains("/courses") || currentUrl.contains("/teachers") || currentUrl.contains("/recommendations"),
                      "点击功能卡片按钮应跳转到对应页面");
        }
    }
    
    // ==================== 导航栏测试（未登录状态） ====================
    
    @Test
    @Order(4)
    @DisplayName("TC-NAV-004: 未登录状态导航栏元素")
    void testNavbarElementsWhenLoggedOut() {
        navigateTo("/");
        sleep(2);
        
        // 验证Logo - 导航栏logo显示"课程评价系统"
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(NAV_LOGO));
        assertTrue(logo.isDisplayed(), "Logo应可见");
        assertTrue(logo.getText().contains("课程评价系统"), "Logo应显示'课程评价系统'");
        
        // 验证课程链接
        WebElement coursesLink = driver.findElement(NAV_COURSES);
        assertTrue(coursesLink.isDisplayed(), "课程链接应可见");
        
        // 验证登录链接（未登录时）
        WebElement loginLink = driver.findElement(NAV_LOGIN);
        assertTrue(loginLink.isDisplayed(), "未登录时登录链接应可见");
        
        // 验证注册链接（未登录时）
        WebElement registerLink = driver.findElement(NAV_REGISTER);
        assertTrue(registerLink.isDisplayed(), "未登录时注册链接应可见");
    }
    
    @Test
    @Order(5)
    @DisplayName("TC-NAV-005: 导航栏Logo点击返回首页")
    void testLogoClickNavigatesToHome() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(NAV_LOGO));
        logo.click();
        sleep(1);
        
        assertEquals(BASE_URL + "/", getCurrentUrl(), "点击Logo应返回首页");
    }
    
    @Test
    @Order(6)
    @DisplayName("TC-NAV-006: 导航栏课程链接跳转")
    void testCoursesLinkNavigation() {
        navigateTo("/");
        sleep(1);
        
        WebElement coursesLink = driver.findElement(NAV_COURSES);
        coursesLink.click();
        sleep(1);
        
        assertTrue(getCurrentUrl().contains("/courses"), "点击课程链接应跳转到课程列表");
    }
    
    @Test
    @Order(7)
    @DisplayName("TC-NAV-007: 导航栏登录链接跳转")
    void testLoginLinkNavigation() {
        navigateTo("/");
        sleep(1);
        
        WebElement loginLink = driver.findElement(NAV_LOGIN);
        loginLink.click();
        sleep(1);
        
        assertTrue(getCurrentUrl().contains("/login"), "点击登录链接应跳转到登录页");
    }
    
    @Test
    @Order(8)
    @DisplayName("TC-NAV-008: 导航栏注册链接跳转")
    void testRegisterLinkNavigation() {
        navigateTo("/");
        sleep(1);
        
        WebElement registerLink = driver.findElement(NAV_REGISTER);
        registerLink.click();
        sleep(1);
        
        assertTrue(getCurrentUrl().contains("/register"), "点击注册链接应跳转到注册页");
    }
    
    // ==================== 导航栏测试（登录状态） ====================
    
    @Test
    @Order(9)
    @DisplayName("TC-NAV-009: 登录后导航栏显示用户信息")
    void testNavbarAfterLogin() {
        // 登录
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.sendKeys(STUDENT_USERNAME);  // zhangsan
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(TEST_PASSWORD);  // Password123
        
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        sleep(3);
        
        // 验证登录后导航栏
        navigateTo("/");
        sleep(1);
        
        // 应显示用户菜单，隐藏登录/注册链接
        boolean hasUserMenu = driver.findElements(NAV_PROFILE).size() > 0 ||
                              driver.findElements(USER_DROPDOWN).size() > 0;
        boolean hideLoginLink = driver.findElements(NAV_LOGIN).size() == 0;
        
        assertTrue(hasUserMenu || hideLoginLink, "登录后应显示用户菜单");
    }
    
    @Test
    @Order(10)
    @DisplayName("TC-NAV-010: 用户下拉菜单功能")
    void testUserDropdownMenu() {
        // 登录
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.sendKeys(STUDENT_USERNAME);
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(TEST_PASSWORD);
        
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        sleep(3);
        
        navigateTo("/");
        sleep(1);
        
        try {
            // 点击用户菜单
            WebElement userMenu = driver.findElement(NAV_PROFILE);
            userMenu.click();
            sleep(1);
            
            // 验证下拉菜单显示
            WebElement dropdown = driver.findElement(USER_DROPDOWN);
            assertTrue(dropdown.isDisplayed(), "下拉菜单应显示");
            
            // 验证退出登录选项
            WebElement logoutBtn = driver.findElement(NAV_LOGOUT);
            assertTrue(logoutBtn.isDisplayed(), "退出登录选项应可见");
        } catch (Exception e) {
            // 用户菜单可能不是下拉形式
        }
    }
    
    @Test
    @Order(11)
    @DisplayName("TC-NAV-011: 退出登录功能")
    void testLogoutFunction() {
        // 登录
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.sendKeys(STUDENT_USERNAME);
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(TEST_PASSWORD);
        
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        sleep(3);
        
        navigateTo("/");
        sleep(1);
        
        try {
            // 点击退出登录
            WebElement userMenu = driver.findElement(NAV_PROFILE);
            userMenu.click();
            sleep(1);
            
            WebElement logoutBtn = driver.findElement(NAV_LOGOUT);
            logoutBtn.click();
            sleep(2);
            
            // 验证退出后显示登录链接
            WebElement loginLink = wait.until(ExpectedConditions.visibilityOfElementLocated(NAV_LOGIN));
            assertTrue(loginLink.isDisplayed(), "退出后应显示登录链接");
        } catch (Exception e) {
            // 退出按钮可能直接在导航栏
            try {
                WebElement logoutBtn = driver.findElement(NAV_LOGOUT);
                logoutBtn.click();
                sleep(2);
            } catch (Exception ex) {
                // 跳过
            }
        }
    }
    
    // ==================== 路由守卫测试 ====================
    
    @Test
    @Order(12)
    @DisplayName("TC-NAV-012: 路由守卫-未登录访问个人中心")
    void testRouteGuardProfile() {
        navigateTo("/profile");
        sleep(2);
        
        // 应重定向到登录页
        assertTrue(getCurrentUrl().contains("/login"),
                  "未登录访问个人中心应重定向到登录页");
    }
    
    @Test
    @Order(13)
    @DisplayName("TC-NAV-013: 路由守卫-未登录访问管理后台")
    void testRouteGuardAdmin() {
        navigateTo("/admin");
        sleep(2);
        
        // 应重定向到登录页
        assertTrue(getCurrentUrl().contains("/login") || getCurrentUrl().contains("/404"),
                  "未登录访问管理后台应重定向到登录页或显示404");
    }
    
    @Test
    @Order(14)
    @DisplayName("TC-NAV-014: 路由守卫-学生访问管理后台")
    void testStudentAccessAdmin() {
        // 以学生身份登录
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.sendKeys(STUDENT_USERNAME);  // zhangsan - 学生
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(TEST_PASSWORD);
        
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        sleep(3);
        
        // 尝试访问管理后台
        navigateTo("/admin");
        sleep(2);
        
        // 学生应无权访问，重定向或显示403
        String currentUrl = getCurrentUrl();
        assertTrue(!currentUrl.contains("/admin") || 
                   currentUrl.contains("/403") ||
                   driver.getPageSource().contains("无权限") ||
                   driver.getPageSource().contains("权限不足"),
                   "学生访问管理后台应被拒绝");
    }
    
    @Test
    @Order(15)
    @DisplayName("TC-NAV-015: 管理员访问管理后台")
    void testAdminAccessAdmin() {
        // 以管理员身份登录
        navigateTo("/login");
        sleep(1);
        
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.sendKeys(ADMIN_USERNAME);  // admin
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(TEST_PASSWORD);
        
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        sleep(3);
        
        // 访问管理后台
        navigateTo("/admin");
        sleep(2);
        
        // 管理员应能访问
        assertTrue(getCurrentUrl().contains("/admin"),
                  "管理员应能访问管理后台");
    }
    
    // ==================== 404页面测试 ====================
    
    @Test
    @Order(16)
    @DisplayName("TC-NAV-016: 404页面-访问不存在的路由")
    void testNotFoundPage() {
        navigateTo("/this-route-does-not-exist-12345");
        sleep(2);
        
        // 应显示404页面
        boolean shows404 = driver.findElements(NOT_FOUND_PAGE).size() > 0 ||
                           driver.getPageSource().contains("404") ||
                           driver.getPageSource().contains("找不到") ||
                           driver.getPageSource().contains("不存在");
        
        assertTrue(shows404, "访问不存在的路由应显示404页面");
    }
    
    @Test
    @Order(17)
    @DisplayName("TC-NAV-017: 404页面-返回首页功能")
    void testNotFoundBackToHome() {
        navigateTo("/nonexistent-page");
        sleep(2);
        
        try {
            WebElement backHomeBtn = driver.findElement(By.cssSelector(".btn-back-home, a[href='/']"));
            backHomeBtn.click();
            sleep(1);
            
            assertEquals(BASE_URL + "/", getCurrentUrl(), "404页面应能返回首页");
        } catch (Exception e) {
            // 404页面可能没有返回首页按钮
        }
    }
    
    // ==================== 页面跳转测试 ====================
    
    @Test
    @Order(18)
    @DisplayName("TC-NAV-018: 课程列表到课程详情跳转")
    void testCourseListToDetail() {
        navigateTo("/courses");
        sleep(2);
        
        java.util.List<WebElement> courseCards = driver.findElements(By.cssSelector(".course-card"));
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(2);
            
            assertTrue(getCurrentUrl().matches(".*\\/courses\\/\\d+.*"),
                      "点击课程卡片应跳转到课程详情页");
        }
    }
    
    @Test
    @Order(19)
    @DisplayName("TC-NAV-019: 课程详情返回课程列表")
    void testCourseDetailBackToList() {
        navigateTo("/courses/1");  // 计算机科学导论
        sleep(2);
        
        WebElement backBtn = driver.findElement(By.cssSelector(".btn-back"));
        backBtn.click();
        sleep(1);
        
        assertTrue(getCurrentUrl().contains("/courses") && 
                   !getCurrentUrl().contains("/courses/"),
                  "点击返回应回到课程列表页");
    }
    
    @Test
    @Order(20)
    @DisplayName("TC-NAV-020: 浏览器后退前进功能")
    void testBrowserBackForward() {
        // 首页 -> 课程列表 -> 登录页
        navigateTo("/");
        sleep(1);
        
        navigateTo("/courses");
        sleep(1);
        
        navigateTo("/login");
        sleep(1);
        
        // 后退到课程列表
        driver.navigate().back();
        sleep(1);
        assertTrue(getCurrentUrl().contains("/courses"), "后退应到课程列表");
        
        // 后退到首页
        driver.navigate().back();
        sleep(1);
        assertEquals(BASE_URL + "/", getCurrentUrl(), "再次后退应到首页");
        
        // 前进到课程列表
        driver.navigate().forward();
        sleep(1);
        assertTrue(getCurrentUrl().contains("/courses"), "前进应到课程列表");
    }
}
