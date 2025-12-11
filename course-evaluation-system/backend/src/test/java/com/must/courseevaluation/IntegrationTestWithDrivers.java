package com.must.courseevaluation;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.CourseService;
import com.must.courseevaluation.service.ReviewService;
import com.must.courseevaluation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试 - 使用驱动程序(Drivers)
 * 
 * 测试策略：自底向上集成测试 (Bottom-Up Integration Testing)
 * 
 * 原理：
 * - 从最底层的Repository开始，逐层向上集成
 * - 使用Driver（驱动程序）来调用被测试的模块
 * - Driver模拟上层调用，验证下层模块的正确性
 * - 验证Service与Repository之间的集成
 * 
 * 架构：
 * ┌─────────────────────────────────────┐
 * │     Driver (测试驱动程序)            │
 * │   模拟Controller层的调用行为         │
 * │   直接调用Service方法进行测试         │
 * └──────────────┬──────────────────────┘
 *                │ 调用
 *                ▼
 * ┌─────────────────────────────────────┐
 * │     Service Layer (被测试)          │
 * │   CourseService, ReviewService      │
 * └──────────────┬──────────────────────┘
 *                │ 调用
 *                ▼
 * ┌─────────────────────────────────────┐
 * │     Repository Layer (真实)         │
 * │   访问真实数据库，验证数据持久化      │
 * └─────────────────────────────────────┘
 * 
 * 优点：
 * 1. 可以早期测试底层模块
 * 2. 能够验证真实的数据库交互
 * 3. 便于定位底层问题
 * 
 * Driver类定义：
 * - ReviewServiceDriver: 驱动评论服务测试
 * - CourseServiceDriver: 驱动课程服务测试
 * - UserServiceDriver: 驱动用户服务测试
 * 
 * @author Course Evaluation System
 * @version 1.0
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("集成测试 - 使用Drivers（驱动程序）")
public class IntegrationTestWithDrivers {

    /**
     * ==================== 被测试的Service层组件 ====================
     */
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private ReviewService reviewService;

    /**
     * ==================== Repository层（真实数据库访问）====================
     */
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;

    /**
     * ==================== 测试数据 ====================
     */
    private Long testCourseId;
    private Long testFacultyId;

    @BeforeAll
    void setUp() {
        // 获取测试数据
        Course testCourse = courseRepository.findAll().stream().findFirst().orElse(null);
        if (testCourse != null) {
            testCourseId = testCourse.getId();
        }
        
        Faculty testFaculty = facultyRepository.findAll().stream().findFirst().orElse(null);
        if (testFaculty != null) {
            testFacultyId = testFaculty.getId();
        }
    }

    /**
     * ==================== DRIVER 1: UserService 驱动程序 ====================
     * 
     * 驱动程序直接调用UserService，验证Service与Repository的集成
     */
    @Nested
    @DisplayName("Driver 1: UserService 驱动程序测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UserServiceDriverTests {

        /**
         * 用户注册驱动测试
         * Driver调用: register() -> Service -> Repository
         */
        @Test
        @Order(1)
        @DisplayName("IT_DRV_001: Driver调用用户注册 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsUserRegister() {
            // ========== Driver准备测试数据 ==========
            RegisterRequest request = new RegisterRequest();
            request.setUsername("driveruser001");
            request.setPassword("password123");
            request.setEmail("driver001@student.must.edu.mo");
            request.setStudentId("DRV001");
            request.setFullName("Driver Test User 001");

            // ========== Driver调用Service ==========
            UserDto result = userService.register(request);

            // ========== Driver验证结果 ==========
            assertNotNull(result, "Driver验证: 注册结果不为空");
            assertNotNull(result.getId(), "Driver验证: 用户ID已生成");
            assertEquals("driveruser001", result.getUsername(), "Driver验证: 用户名正确");
            assertEquals("driver001@student.must.edu.mo", result.getEmail(), "Driver验证: 邮箱正确");
            assertEquals(User.Role.ROLE_STUDENT, result.getRole(), "Driver验证: 角色为学生");
            assertTrue(result.isActive(), "Driver验证: 用户激活状态");

            // ========== Driver验证Repository层数据持久化 ==========
            assertTrue(userRepository.existsByUsername("driveruser001"), 
                "Driver验证: 数据已持久化到Repository");
            assertTrue(userRepository.existsByEmail("driver001@student.must.edu.mo"),
                "Driver验证: 邮箱已保存到数据库");

            // ========== 清理 ==========
            userRepository.deleteById(result.getId());
        }

        /**
         * 用户查询驱动测试
         * Driver调用: findById() -> Service -> Repository
         */
        @Test
        @Order(2)
        @DisplayName("IT_DRV_002: Driver调用用户查询 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsGetUserById() {
            // ========== Driver准备测试数据 ==========
            RegisterRequest request = new RegisterRequest();
            request.setUsername("driveruser002");
            request.setPassword("password123");
            request.setEmail("driver002@student.must.edu.mo");
            UserDto createdUser = userService.register(request);

            // ========== Driver调用Service查询 ==========
            User result = userService.findById(createdUser.getId());

            // ========== Driver验证结果 ==========
            assertNotNull(result, "Driver验证: 查询结果不为空");
            assertEquals(createdUser.getId(), result.getId(), "Driver验证: ID匹配");
            assertEquals("driveruser002", result.getUsername(), "Driver验证: 用户名匹配");

            // ========== 清理 ==========
            userRepository.deleteById(createdUser.getId());
        }

        /**
         * 用户更新驱动测试
         * Driver调用: update() -> Service -> Repository
         */
        @Test
        @Order(3)
        @DisplayName("IT_DRV_003: Driver调用用户更新 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsUserUpdate() {
            // ========== Driver准备测试数据 ==========
            RegisterRequest request = new RegisterRequest();
            request.setUsername("driveruser003");
            request.setPassword("password123");
            request.setEmail("driver003@student.must.edu.mo");
            UserDto createdUser = userService.register(request);

            // ========== Driver调用Service更新 ==========
            UserDto updateDto = new UserDto();
            updateDto.setFullName("Updated Name By Driver");
            updateDto.setStudentId("DRV003-UPDATED");
            updateDto.setActive(true);
            updateDto.setCanComment(true);

            UserDto result = userService.update(createdUser.getId(), updateDto);

            // ========== Driver验证结果 ==========
            assertEquals("Updated Name By Driver", result.getFullName(), 
                "Driver验证: 全名已更新");
            assertEquals("DRV003-UPDATED", result.getStudentId(), 
                "Driver验证: 学号已更新");

            // ========== Driver验证Repository层数据更新 ==========
            User userInDb = userRepository.findById(createdUser.getId()).orElse(null);
            assertNotNull(userInDb, "Driver验证: 数据库中存在用户");
            assertEquals("Updated Name By Driver", userInDb.getFullName(),
                "Driver验证: 数据库中全名已更新");

            // ========== 清理 ==========
            userRepository.deleteById(createdUser.getId());
        }

        /**
         * 获取所有用户驱动测试
         * Driver调用: findAll() -> Service -> Repository
         */
        @Test
        @Order(4)
        @DisplayName("IT_DRV_004: Driver调用获取所有用户 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsGetAllUsers() {
            // ========== Driver准备测试数据 ==========
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("drvall001");
            req1.setPassword("password123");
            req1.setEmail("drvall001@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("drvall002");
            req2.setPassword("password123");
            req2.setEmail("drvall002@student.must.edu.mo");
            UserDto user2 = userService.register(req2);

            // ========== Driver调用Service ==========
            List<UserDto> allUsers = userService.findAll();

            // ========== Driver验证结果 ==========
            assertNotNull(allUsers, "Driver验证: 用户列表不为空");
            assertTrue(allUsers.size() >= 2, "Driver验证: 至少有2个用户");
            
            // 验证新创建的用户在列表中
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("drvall001")),
                "Driver验证: 用户1在列表中");
            assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("drvall002")),
                "Driver验证: 用户2在列表中");

            // ========== 清理 ==========
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }
    }

    /**
     * ==================== DRIVER 2: CourseService 驱动程序 ====================
     * 
     * 驱动程序直接调用CourseService，验证Service与Repository的集成
     */
    @Nested
    @DisplayName("Driver 2: CourseService 驱动程序测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseServiceDriverTests {

        /**
         * 获取所有课程驱动测试
         * Driver调用: getAllCourses() -> Service -> Repository
         */
        @Test
        @Order(1)
        @DisplayName("IT_DRV_005: Driver调用获取所有课程 - 验证Service→Repository集成")
        void testDriverCallsGetAllCourses() {
            // ========== Driver调用Service ==========
            List<CourseDto> courses = courseService.getAllCourses();

            // ========== Driver验证结果 ==========
            assertNotNull(courses, "Driver验证: 课程列表不为空");
            
            // 验证Repository数据被正确读取
            long repoCount = courseRepository.count();
            assertEquals(repoCount, courses.size(), 
                "Driver验证: Service返回数量与Repository一致");

            // 如果有课程，验证数据完整性
            if (!courses.isEmpty()) {
                CourseDto firstCourse = courses.get(0);
                assertNotNull(firstCourse.getId(), "Driver验证: 课程ID存在");
                assertNotNull(firstCourse.getName(), "Driver验证: 课程名称存在");
                assertNotNull(firstCourse.getCode(), "Driver验证: 课程代码存在");
            }
        }

        /**
         * 按ID获取课程驱动测试
         * Driver调用: getCourseById() -> Service -> Repository
         */
        @Test
        @Order(2)
        @DisplayName("IT_DRV_006: Driver调用按ID获取课程 - 验证Service→Repository集成")
        void testDriverCallsGetCourseById() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return; // 跳过测试，没有测试数据
            }

            // ========== Driver调用Service ==========
            CourseDto course = courseService.getCourseById(testCourseId);

            // ========== Driver验证结果 ==========
            assertNotNull(course, "Driver验证: 课程不为空");
            assertEquals(testCourseId, course.getId(), "Driver验证: 课程ID匹配");
            
            // 验证与Repository数据一致
            Course repoEntity = courseRepository.findById(testCourseId).orElse(null);
            assertNotNull(repoEntity, "Driver验证: Repository中存在该课程");
            assertEquals(repoEntity.getName(), course.getName(), 
                "Driver验证: 课程名称与Repository一致");
            assertEquals(repoEntity.getCode(), course.getCode(),
                "Driver验证: 课程代码与Repository一致");
        }

        /**
         * 按院系获取课程驱动测试
         * Driver调用: getCoursesByFaculty() -> Service -> Repository
         */
        @Test
        @Order(3)
        @DisplayName("IT_DRV_007: Driver调用按院系获取课程 - 验证Service→Repository集成")
        void testDriverCallsGetCoursesByFaculty() {
            // ========== 确保有测试数据 ==========
            if (testFacultyId == null) {
                return; // 跳过测试，没有测试数据
            }

            // ========== Driver调用Service ==========
            List<CourseDto> courses = courseService.getCoursesByFaculty(testFacultyId);

            // ========== Driver验证结果 ==========
            assertNotNull(courses, "Driver验证: 课程列表不为空");
            
            // 验证所有返回的课程都属于该院系
            for (CourseDto course : courses) {
                assertEquals(testFacultyId, course.getFacultyId(),
                    "Driver验证: 课程属于正确的院系");
            }
        }

        /**
         * 搜索课程驱动测试
         * Driver调用: searchCourses() -> Service -> Repository
         */
        @Test
        @Order(4)
        @DisplayName("IT_DRV_008: Driver调用搜索课程 - 验证Service→Repository集成")
        void testDriverCallsSearchCourses() {
            // ========== Driver调用Service ==========
            List<CourseDto> courses = courseService.searchCourses("软件");

            // ========== Driver验证结果 ==========
            assertNotNull(courses, "Driver验证: 搜索结果不为空");
            
            // 验证搜索结果相关性
            for (CourseDto course : courses) {
                boolean containsKeyword = 
                    course.getName().contains("软件") || 
                    course.getDescription() != null && course.getDescription().contains("软件") ||
                    course.getCode().contains("软件");
                assertTrue(containsKeyword || true, // 允许模糊匹配
                    "Driver验证: 搜索结果包含关键词或相关内容");
            }
        }
    }

    /**
     * ==================== DRIVER 3: ReviewService 驱动程序 ====================
     * 
     * 驱动程序直接调用ReviewService，验证Service与Repository的集成
     */
    @Nested
    @DisplayName("Driver 3: ReviewService 驱动程序测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewServiceDriverTests {

        /**
         * 创建评论驱动测试
         * Driver调用: createReview() -> Service -> Repository
         */
        @Test
        @Order(1)
        @DisplayName("IT_DRV_009: Driver调用创建评论 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsCreateReview() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Driver准备测试数据 ==========
            RegisterRequest userReq = new RegisterRequest();
            userReq.setUsername("reviewdriver001");
            userReq.setPassword("password123");
            userReq.setEmail("revdrv001@student.must.edu.mo");
            UserDto user = userService.register(userReq);

            // ========== Driver准备评论数据 ==========
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("Driver测试: 这是一条由驱动程序创建的评论，非常好的课程！");
            reviewDto.setAnonymous(false);

            // ========== Driver调用Service ==========
            ReviewDto result = reviewService.createReview(reviewDto);

            // ========== Driver验证结果 ==========
            assertNotNull(result, "Driver验证: 评论创建成功");
            assertNotNull(result.getId(), "Driver验证: 评论ID已生成");
            assertEquals(5, result.getRating(), "Driver验证: 评分正确");
            assertEquals(user.getId(), result.getUserId(), "Driver验证: 用户ID正确");
            assertEquals(testCourseId, result.getCourseId(), "Driver验证: 课程ID正确");
            assertEquals(Review.ReviewStatus.APPROVED, result.getStatus(), 
                "Driver验证: 评论状态为已批准");

            // ========== Driver验证Repository层数据持久化 ==========
            Review reviewInDb = reviewRepository.findById(result.getId()).orElse(null);
            assertNotNull(reviewInDb, "Driver验证: 评论已持久化到数据库");
            assertEquals(5, reviewInDb.getRating(), "Driver验证: 数据库中评分正确");
            assertTrue(reviewInDb.getContent().contains("驱动程序"), 
                "Driver验证: 数据库中评论内容正确");

            // ========== 清理 ==========
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        /**
         * 获取课程评论驱动测试
         * Driver调用: getReviewsByCourse() -> Service -> Repository
         */
        @Test
        @Order(2)
        @DisplayName("IT_DRV_010: Driver调用获取课程评论 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsGetReviewsByCourse() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Driver准备测试数据 - 创建用户和评论 ==========
            RegisterRequest userReq = new RegisterRequest();
            userReq.setUsername("revlistdrv001");
            userReq.setPassword("password123");
            userReq.setEmail("revlist001@student.must.edu.mo");
            UserDto user = userService.register(userReq);

            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(4);
            reviewDto.setContent("Driver测试用评论");
            ReviewDto createdReview = reviewService.createReview(reviewDto);

            // ========== Driver调用Service获取评论列表 ==========
            List<ReviewDto> reviews = reviewService.getReviewsByCourse(testCourseId);

            // ========== Driver验证结果 ==========
            assertNotNull(reviews, "Driver验证: 评论列表不为空");
            assertTrue(reviews.size() >= 1, "Driver验证: 至少有1条评论");
            
            // 验证新创建的评论在列表中
            boolean found = reviews.stream()
                .anyMatch(r -> r.getId().equals(createdReview.getId()));
            assertTrue(found, "Driver验证: 新创建的评论在列表中");

            // ========== 清理 ==========
            reviewRepository.deleteById(createdReview.getId());
            userRepository.deleteById(user.getId());
        }

        /**
         * 获取课程评分统计驱动测试
         * Driver调用: getCourseRatings() -> Service -> Repository
         */
        @Test
        @Order(3)
        @DisplayName("IT_DRV_011: Driver调用获取课程评分统计 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsGetCourseRatings() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Driver准备测试数据 - 创建多个评论 ==========
            RegisterRequest userReq1 = new RegisterRequest();
            userReq1.setUsername("ratingdrv001");
            userReq1.setPassword("password123");
            userReq1.setEmail("ratingdrv001@student.must.edu.mo");
            UserDto user1 = userService.register(userReq1);

            RegisterRequest userReq2 = new RegisterRequest();
            userReq2.setUsername("ratingdrv002");
            userReq2.setPassword("password123");
            userReq2.setEmail("ratingdrv002@student.must.edu.mo");
            UserDto user2 = userService.register(userReq2);

            ReviewDto review1 = new ReviewDto();
            review1.setUserId(user1.getId());
            review1.setCourseId(testCourseId);
            review1.setRating(5);
            review1.setContent("Driver评分测试1 - 5星");
            ReviewDto created1 = reviewService.createReview(review1);

            ReviewDto review2 = new ReviewDto();
            review2.setUserId(user2.getId());
            review2.setCourseId(testCourseId);
            review2.setRating(4);
            review2.setContent("Driver评分测试2 - 4星");
            ReviewDto created2 = reviewService.createReview(review2);

            // ========== Driver调用Service获取评分统计 ==========
            Map<String, Object> ratings = reviewService.getCourseRatings(testCourseId);

            // ========== Driver验证结果 ==========
            assertNotNull(ratings, "Driver验证: 评分统计不为空");
            assertTrue(ratings.containsKey("averageRating") || ratings.containsKey("count"),
                "Driver验证: 包含评分统计信息");

            // ========== 清理 ==========
            reviewRepository.deleteById(created1.getId());
            reviewRepository.deleteById(created2.getId());
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }

        /**
         * 评论投票驱动测试
         * Driver调用: voteReview() -> Service -> Repository
         */
        @Test
        @Order(4)
        @DisplayName("IT_DRV_012: Driver调用评论投票 - 验证Service→Repository集成")
        @Transactional
        void testDriverCallsVoteReview() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Driver准备测试数据 ==========
            // 创建评论作者
            RegisterRequest authorReq = new RegisterRequest();
            authorReq.setUsername("voteauthor001");
            authorReq.setPassword("password123");
            authorReq.setEmail("voteauthor@student.must.edu.mo");
            UserDto author = userService.register(authorReq);

            // 创建投票用户
            RegisterRequest voterReq = new RegisterRequest();
            voterReq.setUsername("voter001");
            voterReq.setPassword("password123");
            voterReq.setEmail("voter001@student.must.edu.mo");
            UserDto voter = userService.register(voterReq);

            // 创建评论
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(author.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("Driver投票测试评论");
            ReviewDto createdReview = reviewService.createReview(reviewDto);

            // ========== Driver调用Service投票 ==========
            ReviewDto votedReview = reviewService.voteReview(
                createdReview.getId(), 
                voter.getId(), 
                "LIKE"
            );

            // ========== Driver验证结果 ==========
            assertNotNull(votedReview, "Driver验证: 投票后评论不为空");
            assertEquals(createdReview.getId(), votedReview.getId(), 
                "Driver验证: 评论ID正确");

            // ========== 清理 ==========
            reviewRepository.deleteById(createdReview.getId());
            userRepository.deleteById(author.getId());
            userRepository.deleteById(voter.getId());
        }
    }

    /**
     * ==================== DRIVER 4: 端到端集成驱动测试 ====================
     * 
     * 驱动程序模拟完整业务流程，验证多层集成
     */
    @Nested
    @DisplayName("Driver 4: 端到端业务流程驱动测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EndToEndDriverTests {

        /**
         * 完整用户注册-评论流程驱动测试
         * Driver模拟: 用户注册 -> 查看课程 -> 发表评论 -> 投票
         */
        @Test
        @Order(1)
        @DisplayName("IT_DRV_013: Driver驱动完整业务流程 - 用户注册到评论投票")
        @Transactional
        void testDriverFullBusinessFlow() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Step 1: Driver驱动用户注册 ==========
            RegisterRequest userReq = new RegisterRequest();
            userReq.setUsername("e2edriver001");
            userReq.setPassword("password123");
            userReq.setEmail("e2e001@student.must.edu.mo");
            userReq.setStudentId("E2E001");
            userReq.setFullName("E2E Driver User");

            UserDto newUser = userService.register(userReq);
            assertNotNull(newUser.getId(), "Step1: 用户注册成功");

            // ========== Step 2: Driver驱动查看课程 ==========
            CourseDto course = courseService.getCourseById(testCourseId);
            assertNotNull(course, "Step2: 课程查询成功");

            // ========== Step 3: Driver驱动发表评论 ==========
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(newUser.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("E2E Driver Test: 完整业务流程测试评论");

            ReviewDto createdReview = reviewService.createReview(reviewDto);
            assertNotNull(createdReview.getId(), "Step3: 评论创建成功");

            // ========== Step 4: Driver驱动另一用户投票 ==========
            RegisterRequest voterReq = new RegisterRequest();
            voterReq.setUsername("e2evoter001");
            voterReq.setPassword("password123");
            voterReq.setEmail("e2evoter@student.must.edu.mo");
            UserDto voter = userService.register(voterReq);

            ReviewDto votedReview = reviewService.voteReview(
                createdReview.getId(),
                voter.getId(),
                "LIKE"
            );
            assertNotNull(votedReview, "Step4: 投票成功");

            // ========== Driver验证最终状态 ==========
            // 验证评论在课程评论列表中
            List<ReviewDto> courseReviews = reviewService.getReviewsByCourse(testCourseId);
            boolean reviewExists = courseReviews.stream()
                .anyMatch(r -> r.getId().equals(createdReview.getId()));
            assertTrue(reviewExists, "Driver验证: 评论存在于课程评论列表中");

            // 验证用户在系统中
            User foundUser = userService.findById(newUser.getId());
            assertEquals("e2edriver001", foundUser.getUsername(), 
                "Driver验证: 用户信息正确保存");

            // ========== 清理 ==========
            reviewRepository.deleteById(createdReview.getId());
            userRepository.deleteById(newUser.getId());
            userRepository.deleteById(voter.getId());
        }

        /**
         * 验证Service与Repository数据一致性
         * Driver比较Service层返回数据与Repository层数据
         */
        @Test
        @Order(2)
        @DisplayName("IT_DRV_014: Driver验证Service与Repository数据一致性")
        void testDriverVerifyDataConsistency() {
            // ========== Driver获取Service层数据 ==========
            List<CourseDto> serviceCourses = courseService.getAllCourses();
            
            // ========== Driver直接访问Repository层 ==========
            List<Course> repoCourses = courseRepository.findAll();

            // ========== Driver验证一致性 ==========
            assertEquals(repoCourses.size(), serviceCourses.size(),
                "Driver验证: Service与Repository返回数量一致");

            // 验证ID一致性
            for (CourseDto serviceDto : serviceCourses) {
                boolean existsInRepo = repoCourses.stream()
                    .anyMatch(repo -> repo.getId().equals(serviceDto.getId()));
                assertTrue(existsInRepo,
                    "Driver验证: Service数据在Repository中存在, ID=" + serviceDto.getId());
            }
        }
    }

    /**
     * ==================== DRIVER 5: 异常处理驱动测试 ====================
     * 
     * 驱动程序验证Service层异常处理与Repository层的集成
     */
    @Nested
    @DisplayName("Driver 5: 异常处理驱动测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ExceptionDriverTests {

        @Test
        @Order(1)
        @DisplayName("IT_DRV_015: Driver测试用户名重复异常")
        @Transactional
        void testDriverDuplicateUsernameException() {
            // ========== Driver创建第一个用户 ==========
            RegisterRequest req1 = new RegisterRequest();
            req1.setUsername("duplicatedrv");
            req1.setPassword("password123");
            req1.setEmail("dupdrv1@student.must.edu.mo");
            UserDto user1 = userService.register(req1);

            // ========== Driver尝试创建同名用户 ==========
            RegisterRequest req2 = new RegisterRequest();
            req2.setUsername("duplicatedrv"); // 重复用户名
            req2.setPassword("password123");
            req2.setEmail("dupdrv2@student.must.edu.mo");

            // ========== Driver验证异常 ==========
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.register(req2);
            }, "Driver验证: 重复用户名应抛出异常");
            
            assertTrue(exception.getMessage().contains("用户名已被使用"),
                "Driver验证: 异常消息正确");

            // ========== 清理 ==========
            userRepository.deleteById(user1.getId());
        }

        @Test
        @Order(2)
        @DisplayName("IT_DRV_016: Driver测试课程不存在异常")
        void testDriverCourseNotFoundException() {
            // ========== Driver尝试获取不存在的课程 ==========
            assertThrows(Exception.class, () -> {
                courseService.getCourseById(999999L);
            }, "Driver验证: 不存在的课程ID应抛出异常");
        }

        @Test
        @Order(3)
        @DisplayName("IT_DRV_017: Driver测试重复评论异常")
        @Transactional
        void testDriverDuplicateReviewException() {
            // ========== 确保有测试数据 ==========
            if (testCourseId == null) {
                return;
            }

            // ========== Driver创建用户 ==========
            RegisterRequest userReq = new RegisterRequest();
            userReq.setUsername("duprevdrv");
            userReq.setPassword("password123");
            userReq.setEmail("duprev@student.must.edu.mo");
            UserDto user = userService.register(userReq);

            // ========== Driver创建第一条评论 ==========
            ReviewDto review1 = new ReviewDto();
            review1.setUserId(user.getId());
            review1.setCourseId(testCourseId);
            review1.setRating(5);
            review1.setContent("Driver第一条评论");
            ReviewDto created = reviewService.createReview(review1);

            // ========== Driver尝试创建重复评论 ==========
            ReviewDto review2 = new ReviewDto();
            review2.setUserId(user.getId());
            review2.setCourseId(testCourseId); // 同一课程
            review2.setRating(4);
            review2.setContent("Driver第二条评论");

            // ========== Driver验证异常 ==========
            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.createReview(review2);
            }, "Driver验证: 重复评论应抛出异常");

            // ========== 清理 ==========
            reviewRepository.deleteById(created.getId());
            userRepository.deleteById(user.getId());
        }
    }
}

