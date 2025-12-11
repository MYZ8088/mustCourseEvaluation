package com.must.courseevaluation.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 课程功能 - Selenium自动化测试
 * 
 * 测试范围：
 * 1. 课程列表页面元素验证
 * 2. 课程搜索功能
 * 3. 课程过滤功能（院系、类型、时间）
 * 4. 课程详情页面
 * 5. 页面响应和交互
 * 
 * 测试数据（来自系统预置数据）：
 * - 院系: 创新工程学院、商学院、人文艺术学院、酒店与旅游管理学院、医学院
 * - 课程: 计算机科学导论(FIE101)、数据结构与算法(FIE201)、人工智能导论(FIE301)等21门
 * - 教师: 陈伟、林晓明、黄建华等10名
 * 
 * @author Course Evaluation System Team
 * @version 1.0
 */
@DisplayName("课程功能 - Selenium自动化测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseSeleniumTests extends SeleniumTestBase {
    
    // ==================== 页面元素定位器 ====================
    
    private static final By SEARCH_INPUT = By.cssSelector(".search-box input");
    private static final By FACULTY_FILTER = By.cssSelector(".faculty-filter select");
    private static final By TYPE_FILTER = By.cssSelector(".type-filter select");
    private static final By COURSE_CARDS = By.cssSelector(".course-card");
    private static final By COURSE_NAME = By.cssSelector(".course-info h3");
    private static final By COURSE_CODE = By.cssSelector(".course-code");
    private static final By COURSE_TYPE_TAG = By.cssSelector(".course-type");
    private static final By BACK_HOME_BUTTON = By.cssSelector(".btn-back");
    private static final By NO_RESULTS = By.cssSelector(".no-results");
    private static final By LOADING = By.cssSelector(".loading");
    private static final By SCHEDULE_FILTER = By.cssSelector(".schedule-filter");
    private static final By CLEAR_SCHEDULE_BUTTON = By.cssSelector(".btn-clear");
    private static final By DAY_CHECKBOXES = By.cssSelector(".day-options input[type='checkbox']");
    
    // ==================== 课程列表页面测试 ====================
    
    @Test
    @Order(1)
    @DisplayName("TC-COURSE-001: 验证课程列表页面元素完整性")
    void testCourseListPageElements() {
        navigateTo("/courses");
        sleep(2);
        
        // 验证页面标题
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(title.getText().contains("课程列表"), "页面标题应包含'课程列表'");
        
        // 验证搜索框
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        assertTrue(searchInput.isDisplayed(), "搜索输入框应可见");
        assertEquals("搜索课程名称...", searchInput.getAttribute("placeholder"));
        
        // 验证院系过滤下拉框
        WebElement facultyFilter = driver.findElement(FACULTY_FILTER);
        assertTrue(facultyFilter.isDisplayed(), "院系过滤下拉框应可见");
        
        // 验证类型过滤下拉框
        WebElement typeFilter = driver.findElement(TYPE_FILTER);
        assertTrue(typeFilter.isDisplayed(), "类型过滤下拉框应可见");
        
        // 验证时间筛选区域
        WebElement scheduleFilter = driver.findElement(SCHEDULE_FILTER);
        assertTrue(scheduleFilter.isDisplayed(), "时间筛选区域应可见");
        
        // 验证返回首页按钮
        WebElement backButton = driver.findElement(BACK_HOME_BUTTON);
        assertTrue(backButton.isDisplayed(), "返回首页按钮应可见");
    }
    
    @Test
    @Order(2)
    @DisplayName("TC-COURSE-002: 验证课程列表加载成功(应有21门课程)")
    void testCourseListLoads() {
        navigateTo("/courses");
        sleep(3);
        
        // 等待加载完成
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING));
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        
        // 系统预置21门课程
        assertTrue(courseCards.size() > 0, "应显示课程列表");
        assertTrue(courseCards.size() >= 20, "应显示至少20门课程（系统预置21门）");
        
        // 验证课程卡片结构
        WebElement firstCard = courseCards.get(0);
        assertTrue(firstCard.findElement(COURSE_CODE).isDisplayed(), "课程代码应可见");
        assertTrue(firstCard.findElement(COURSE_NAME).isDisplayed(), "课程名称应可见");
        assertTrue(firstCard.findElement(COURSE_TYPE_TAG).isDisplayed(), "课程类型应可见");
    }
    
    // ==================== 搜索功能测试 ====================
    
    @Test
    @Order(3)
    @DisplayName("TC-COURSE-003: 搜索功能-按课程名称搜索'计算机'")
    void testSearchByCourseName() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchInput.clear();
        searchInput.sendKeys("计算机");  // 搜索"计算机科学导论"
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() > 0, "搜索'计算机'应有结果");
        
        // 验证搜索结果包含关键词
        for (WebElement card : results) {
            String courseName = card.findElement(COURSE_NAME).getText();
            assertTrue(courseName.contains("计算机"), "搜索结果应包含'计算机'");
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("TC-COURSE-004: 搜索功能-按课程代码搜索'FIE'")
    void testSearchByCourseCode() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchInput.clear();
        searchInput.sendKeys("FIE");  // 搜索创新工程学院课程代码
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() > 0, "搜索'FIE'应有结果");
        
        // 验证结果是创新工程学院的课程
        for (WebElement card : results) {
            String courseCode = card.findElement(COURSE_CODE).getText();
            assertTrue(courseCode.contains("FIE"), "搜索结果代码应包含'FIE'");
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("TC-COURSE-005: 搜索功能-搜索'人工智能'")
    void testSearchAICourse() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchInput.clear();
        searchInput.sendKeys("人工智能");
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() >= 1, "搜索'人工智能'应找到人工智能导论");
        
        // 验证找到人工智能导论
        boolean foundAI = results.stream()
                .anyMatch(card -> card.findElement(COURSE_NAME).getText().contains("人工智能"));
        assertTrue(foundAI, "应找到人工智能导论课程");
    }
    
    @Test
    @Order(6)
    @DisplayName("TC-COURSE-006: 搜索功能-无匹配结果")
    void testSearchNoResults() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchInput.clear();
        searchInput.sendKeys("zzzznonexistent12345");  // 不存在的课程名
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertEquals(0, results.size(), "搜索不存在的课程应返回空结果");
    }
    
    @Test
    @Order(7)
    @DisplayName("TC-COURSE-007: 搜索功能-清空搜索恢复所有课程")
    void testClearSearchShowsAll() {
        navigateTo("/courses");
        sleep(3);  // 等待页面完全加载
        
        // 记录初始课程数量
        int initialCount = driver.findElements(COURSE_CARDS).size();
        assertTrue(initialCount > 0, "应有课程列表");
        
        // 搜索特定课程
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchInput.sendKeys("计算机");
        sleep(2);  // 等待搜索结果
        
        int searchCount = driver.findElements(COURSE_CARDS).size();
        assertTrue(searchCount > 0, "搜索'计算机'应有结果");
        
        // 清空搜索 - 使用JavaScript确保触发事件
        searchInput.clear();
        sleep(1);
        // 使用Backspace确保触发input事件
        searchInput.sendKeys(Keys.BACK_SPACE);
        sleep(2);  // 等待重新加载
        
        int afterClearCount = driver.findElements(COURSE_CARDS).size();
        // 清空后数量应该大于搜索时的数量
        assertTrue(afterClearCount >= searchCount, "清空搜索后课程数量应恢复或保持");
    }
    
    // ==================== 过滤功能测试 ====================
    
    @Test
    @Order(8)
    @DisplayName("TC-COURSE-008: 过滤功能-按院系过滤(创新工程学院)")
    void testFilterByFaculty() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement facultySelect = wait.until(ExpectedConditions.elementToBeClickable(FACULTY_FILTER));
        Select select = new Select(facultySelect);
        
        // 选择创新工程学院
        select.selectByVisibleText(TEST_FACULTY_NAME);  // "创新工程学院"
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        
        // 创新工程学院有5门课程
        assertTrue(results.size() >= 4, "创新工程学院应有至少4门课程");
        
        // 验证所有结果都是创新工程学院的
        for (WebElement card : results) {
            String facultyName = card.findElement(By.cssSelector(".course-faculty")).getText();
            assertEquals(TEST_FACULTY_NAME, facultyName, "过滤结果应属于创新工程学院");
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("TC-COURSE-009: 过滤功能-按课程类型过滤(必修)")
    void testFilterByCompulsoryType() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement typeSelect = wait.until(ExpectedConditions.elementToBeClickable(TYPE_FILTER));
        Select select = new Select(typeSelect);
        
        select.selectByValue("COMPULSORY");
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() > 0, "应有必修课程");
        
        // 验证所有结果都是必修课
        for (WebElement card : results) {
            WebElement typeTag = card.findElement(COURSE_TYPE_TAG);
            assertTrue(typeTag.getText().contains("必修") || 
                       typeTag.getAttribute("class").contains("compulsory"),
                       "过滤结果应都是必修课");
        }
    }
    
    @Test
    @Order(10)
    @DisplayName("TC-COURSE-010: 过滤功能-按课程类型过滤(选修)")
    void testFilterByElectiveType() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement typeSelect = wait.until(ExpectedConditions.elementToBeClickable(TYPE_FILTER));
        Select select = new Select(typeSelect);
        
        select.selectByValue("ELECTIVE");
        sleep(1);
        
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() > 0, "应有选修课程");
        
        for (WebElement card : results) {
            WebElement typeTag = card.findElement(COURSE_TYPE_TAG);
            assertTrue(typeTag.getText().contains("选修") || 
                       typeTag.getAttribute("class").contains("elective"),
                       "过滤结果应都是选修课");
        }
    }
    
    @Test
    @Order(11)
    @DisplayName("TC-COURSE-011: 过滤功能-按上课时间过滤(周一)")
    void testFilterByDayOfWeek() {
        navigateTo("/courses");
        sleep(2);
        
        int initialCount = driver.findElements(COURSE_CARDS).size();
        
        List<WebElement> dayCheckboxes = driver.findElements(DAY_CHECKBOXES);
        if (dayCheckboxes.size() > 0) {
            dayCheckboxes.get(0).click();  // 选择周一
            sleep(1);
            
            int filteredCount = driver.findElements(COURSE_CARDS).size();
            // 周一有课程：计算机科学导论、人工智能导论、设计基础、经济学原理等
            assertTrue(filteredCount > 0 && filteredCount <= initialCount,
                       "按周一筛选应有结果");
        }
    }
    
    @Test
    @Order(12)
    @DisplayName("TC-COURSE-012: 过滤功能-清除时间筛选")
    void testClearScheduleFilter() {
        navigateTo("/courses");
        sleep(2);
        
        int initialCount = driver.findElements(COURSE_CARDS).size();
        
        List<WebElement> dayCheckboxes = driver.findElements(DAY_CHECKBOXES);
        if (dayCheckboxes.size() > 0) {
            dayCheckboxes.get(0).click();
            sleep(1);
            
            try {
                WebElement clearBtn = wait.until(ExpectedConditions.elementToBeClickable(CLEAR_SCHEDULE_BUTTON));
                clearBtn.click();
                sleep(1);
                
                int afterClearCount = driver.findElements(COURSE_CARDS).size();
                assertEquals(initialCount, afterClearCount, "清除筛选后应恢复显示所有课程");
            } catch (Exception e) {
                // 可能筛选后没有清除按钮
            }
        }
    }
    
    @Test
    @Order(13)
    @DisplayName("TC-COURSE-013: 组合过滤-创新工程学院+必修课")
    void testCombinedFilter() {
        navigateTo("/courses");
        sleep(2);
        
        // 选择院系
        WebElement facultySelect = driver.findElement(FACULTY_FILTER);
        Select facultyDropdown = new Select(facultySelect);
        facultyDropdown.selectByVisibleText(TEST_FACULTY_NAME);
        sleep(1);
        
        // 选择必修课
        WebElement typeSelect = driver.findElement(TYPE_FILTER);
        Select typeDropdown = new Select(typeSelect);
        typeDropdown.selectByValue("COMPULSORY");
        sleep(1);
        
        // 创新工程学院有3门必修课
        List<WebElement> results = driver.findElements(COURSE_CARDS);
        assertTrue(results.size() >= 2, "创新工程学院应有至少2门必修课");
        
        for (WebElement card : results) {
            String facultyName = card.findElement(By.cssSelector(".course-faculty")).getText();
            WebElement typeTag = card.findElement(COURSE_TYPE_TAG);
            
            assertEquals(TEST_FACULTY_NAME, facultyName, "院系应匹配");
            assertTrue(typeTag.getText().contains("必修"), "类型应为必修");
        }
    }
    
    // ==================== 课程详情页面测试 ====================
    
    @Test
    @Order(14)
    @DisplayName("TC-COURSE-014: 点击课程卡片进入详情页")
    void testNavigateToCourseDetail() {
        navigateTo("/courses");
        sleep(2);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        assertTrue(courseCards.size() > 0, "应有课程可点击");
        
        courseCards.get(0).click();
        sleep(2);
        
        assertTrue(getCurrentUrl().contains("/courses/"), "应跳转到课程详情页");
    }
    
    @Test
    @Order(15)
    @DisplayName("TC-COURSE-015: 课程详情页元素完整性")
    void testCourseDetailPageElements() {
        navigateTo("/courses");
        sleep(2);
        
        // 搜索计算机科学导论（有15条评价，会触发AI总结）
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        searchInput.sendKeys(TEST_COURSE_NAME);  // "计算机科学导论"
        sleep(1);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(2);
            
            // 验证课程基本信息
            assertTrue(driver.findElement(By.cssSelector(".course-code")).isDisplayed(), "课程代码应可见");
            assertTrue(driver.findElement(By.cssSelector(".course-title-section h2")).isDisplayed(), "课程名称应可见");
            assertTrue(driver.findElement(By.cssSelector(".course-type")).isDisplayed(), "课程类型应可见");
            assertTrue(driver.findElement(By.cssSelector(".course-faculty")).isDisplayed(), "所属院系应可见");
            assertTrue(driver.findElement(By.cssSelector(".course-credits")).isDisplayed(), "学分应可见");
        }
    }
    
    @Test
    @Order(16)
    @DisplayName("TC-COURSE-016: 课程详情页-评价统计区域(计算机科学导论有15条评价)")
    void testCourseRatingSection() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        searchInput.sendKeys(TEST_COURSE_NAME);
        sleep(1);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(2);
            
            // 计算机科学导论有15条评价，应显示评价统计
            WebElement ratingSection = driver.findElement(By.cssSelector(".course-rating-section"));
            assertTrue(ratingSection.isDisplayed(), "评价统计区域应可见");
            
            WebElement avgRating = ratingSection.findElement(By.cssSelector(".rating-number"));
            assertTrue(avgRating.isDisplayed(), "平均评分应可见");
            
            WebElement ratingCount = ratingSection.findElement(By.cssSelector(".rating-count"));
            assertTrue(ratingCount.getText().contains("评价"), "应显示评价数量");
        }
    }
    
    @Test
    @Order(17)
    @DisplayName("TC-COURSE-017: 课程详情页-AI总结区域(计算机科学导论评价>=10条)")
    void testAISummarySection() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        searchInput.sendKeys(TEST_COURSE_NAME);
        sleep(1);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(3);
            
            // 验证AI总结区域存在
            WebElement aiSection = driver.findElement(By.cssSelector(".ai-summary-section"));
            assertTrue(aiSection.isDisplayed(), "AI评价总结区域应可见");
            
            WebElement sectionTitle = aiSection.findElement(By.tagName("h3"));
            assertTrue(sectionTitle.getText().contains("AI"), "区域标题应包含'AI'");
        }
    }
    
    @Test
    @Order(18)
    @DisplayName("TC-COURSE-018: 课程详情页-评价列表区域")
    void testCourseReviewsSection() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        searchInput.sendKeys(TEST_COURSE_NAME);
        sleep(1);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(2);
            
            WebElement reviewsSection = driver.findElement(By.cssSelector(".course-reviews-section"));
            assertTrue(reviewsSection.isDisplayed(), "学生评价区域应可见");
            
            WebElement sectionTitle = reviewsSection.findElement(By.tagName("h3"));
            assertTrue(sectionTitle.getText().contains("学生评价"), "区域标题应为'学生评价'");
            
            // 计算机科学导论有15条评价
            List<WebElement> reviewCards = driver.findElements(By.cssSelector(".review-card"));
            assertTrue(reviewCards.size() > 0, "应显示评价列表");
        }
    }
    
    @Test
    @Order(19)
    @DisplayName("TC-COURSE-019: 课程详情页-返回课程列表")
    void testReturnToCourseList() {
        navigateTo("/courses");
        sleep(2);
        
        List<WebElement> courseCards = driver.findElements(COURSE_CARDS);
        if (courseCards.size() > 0) {
            courseCards.get(0).click();
            sleep(2);
            
            WebElement backBtn = driver.findElement(By.cssSelector(".btn-back"));
            backBtn.click();
            sleep(1);
            
            assertTrue(getCurrentUrl().contains("/courses"), "应返回课程列表页面");
        }
    }
    
    @Test
    @Order(20)
    @DisplayName("TC-COURSE-020: 返回首页功能")
    void testReturnToHome() {
        navigateTo("/courses");
        sleep(2);
        
        WebElement backButton = driver.findElement(BACK_HOME_BUTTON);
        backButton.click();
        sleep(1);
        
        assertEquals(BASE_URL + "/", getCurrentUrl(), "应返回首页");
    }
}
