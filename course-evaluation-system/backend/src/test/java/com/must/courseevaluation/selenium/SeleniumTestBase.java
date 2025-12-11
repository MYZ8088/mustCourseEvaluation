package com.must.courseevaluation.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Selenium自动化测试基类
 * 
 * 提供WebDriver初始化、浏览器配置和通用工具方法
 * 所有Selenium测试类应继承此类
 * 
 * @author Course Evaluation System Team
 * @version 1.0
 */
public abstract class SeleniumTestBase {
    
    /** WebDriver实例 */
    protected WebDriver driver;
    
    /** 显式等待工具，默认10秒超时 */
    protected WebDriverWait wait;
    
    /** 前端应用基础URL */
    protected static final String BASE_URL = "http://localhost:8080";
    
    /** API基础URL */
    protected static final String API_URL = "http://localhost:8088";
    
    /** 默认等待超时时间（秒） */
    protected static final int DEFAULT_TIMEOUT = 10;
    
    /** 短等待时间（秒） */
    protected static final int SHORT_TIMEOUT = 3;
    
    // ==================== 系统真实测试数据 ====================
    // 所有账户密码都是: Password123
    
    /** 管理员用户名 */
    protected static final String ADMIN_USERNAME = "admin";
    
    /** 学生用户名 (zhangsan) */
    protected static final String STUDENT_USERNAME = "zhangsan";
    
    /** 学生用户名2 (lisi) */
    protected static final String STUDENT_USERNAME_2 = "lisi";
    
    /** 统一测试密码 */
    protected static final String TEST_PASSWORD = "Password123";
    
    /** 管理员邮箱 */
    protected static final String ADMIN_EMAIL = "admin@must.edu.mo";
    
    /** 学生邮箱 */
    protected static final String STUDENT_EMAIL = "zhangsan@student.must.edu.mo";
    
    // ==================== 课程测试数据 ====================
    
    /** 测试课程名称 - 计算机科学导论 (有15条评价) */
    protected static final String TEST_COURSE_NAME = "计算机科学导论";
    
    /** 测试课程代码 */
    protected static final String TEST_COURSE_CODE = "FIE101";
    
    /** 测试院系名称 */
    protected static final String TEST_FACULTY_NAME = "创新工程学院";
    
    /** 测试教师名称 */
    protected static final String TEST_TEACHER_NAME = "陈伟";
    
    /**
     * 在所有测试之前设置WebDriverManager
     * 自动下载和配置ChromeDriver
     */
    @BeforeAll
    static void setupWebDriverManager() {
        WebDriverManager.chromedriver().setup();
    }
    
    /**
     * 每个测试方法之前初始化WebDriver
     * 配置Chrome浏览器选项
     */
    @BeforeEach
    void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        
        // 无头模式配置（适用于CI/CD环境）
        // options.addArguments("--headless=new");
        
        // 常用配置选项
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-notifications");
        
        // 中文支持
        options.addArguments("--lang=zh-CN");
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(SHORT_TIMEOUT));
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }
    
    /**
     * 每个测试方法之后关闭WebDriver
     */
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * 导航到指定路径
     * @param path 相对路径（如 "/login"）
     */
    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
    }
    
    /**
     * 等待指定时间
     * @param seconds 等待秒数
     */
    protected void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取当前页面标题
     * @return 页面标题
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }
    
    /**
     * 获取当前URL
     * @return 当前页面URL
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    /**
     * 截图保存（用于调试）
     * @param fileName 文件名
     */
    protected void takeScreenshot(String fileName) {
        // 实现截图逻辑
        // 可以使用 TakesScreenshot 接口
    }
}

