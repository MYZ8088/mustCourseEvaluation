package com.must.courseevaluation.whitebox;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.ReviewService;
import com.must.courseevaluation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评论服务模块白盒测试
 * 
 * 测试功能：创建评论 (createReview方法)
 * 
 * 源代码分析：
 * - 判定D1 (复合条件): user.getRole() != ROLE_ADMIN && existsByUserAndCourse()
 *   - C1: user.getRole() != ROLE_ADMIN
 *   - C2: existsByUserAndCourse()
 * - 环路复杂度: V(G) = 4 (考虑用户不存在、课程不存在、D1、成功路径)
 * 
 * 测试方法：
 * 1. 逻辑覆盖法
 *    - 语句覆盖
 *    - 判定覆盖
 *    - 条件覆盖
 *    - 判定/条件覆盖
 *    - 条件组合覆盖
 * 2. 基本路径法
 *    - 独立路径识别
 *    - 路径覆盖测试
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("评论服务 - 白盒测试")
public class ReviewServiceWhiteBoxTests {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Long testCourseId;

    @BeforeAll
    void setUp() {
        // 准备测试课程
        Course testCourse = courseRepository.findAll().stream().findFirst().orElse(null);
        if (testCourse != null) {
            testCourseId = testCourse.getId();
        }
    }

    /**
     * 1.1 语句覆盖测试 - 创建评论
     * 目标：覆盖所有可执行语句
     * 覆盖率：100%
     */
    @Nested
    @DisplayName("1.1 创建评论 - 语句覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateReviewStatementCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_RC_ST_001: 所有语句覆盖 - 学生首次评论成功")
        @Transactional
        void testAllStatementsCovered() {
            // 准备 - 创建学生用户
            RegisterRequest req = new RegisterRequest();
            req.setUsername("stuser001");
            req.setPassword("password123");
            req.setEmail("st001@student.must.edu.mo");
            var user = userService.register(req);

            // 准备评论数据
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("很好的课程，推荐！");
            reviewDto.setAnonymous(false);

            // 执行
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(5, result.getRating());
            assertTrue(result.getContent().contains("课程"));
            assertEquals(Review.ReviewStatus.APPROVED, result.getStatus());

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 1.2 判定覆盖测试 - 创建评论
     * 复合判定D1: C1 && C2
     * - C1: user.getRole() != ROLE_ADMIN
     * - C2: existsByUserAndCourse()
     */
    @Nested
    @DisplayName("1.2 创建评论 - 判定覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateReviewDecisionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_RC_DC_001: D1=True - 学生重复评论（抛出异常）")
        @Transactional
        void testD1True_StudentDuplicateReview() {
            // 准备 - 创建学生并发表评论
            RegisterRequest req = new RegisterRequest();
            req.setUsername("dupuser001");
            req.setPassword("password123");
            req.setEmail("dup001@student.must.edu.mo");
            var user = userService.register(req);

            ReviewDto firstReview = new ReviewDto();
            firstReview.setUserId(user.getId());
            firstReview.setCourseId(testCourseId);
            firstReview.setRating(5);
            firstReview.setContent("第一次评论");
            ReviewDto savedReview = reviewService.createReview(firstReview);

            // 测试 - 同一学生再次评论同一课程
            ReviewDto secondReview = new ReviewDto();
            secondReview.setUserId(user.getId());
            secondReview.setCourseId(testCourseId);
            secondReview.setRating(4);
            secondReview.setContent("第二次评论");

            // 验证 - D1为True，应该抛出异常
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                reviewService.createReview(secondReview);
            });
            assertTrue(exception.getMessage().contains("已经对这门课程发表过评价"));

            // 清理
            reviewRepository.deleteById(savedReview.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_RC_DC_002: D1=False - 学生首次评论成功")
        @Transactional
        void testD1False_StudentFirstReview() {
            // 准备 - 创建新学生
            RegisterRequest req = new RegisterRequest();
            req.setUsername("firstuser002");
            req.setPassword("password123");
            req.setEmail("first002@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 首次评论
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(4);
            reviewDto.setContent("很好的课程");

            // 执行 - D1为False，允许创建
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(4, result.getRating());

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 1.3 条件覆盖测试 - 创建评论
     * 确保C1和C2的真假值都出现
     */
    @Nested
    @DisplayName("1.3 创建评论 - 条件覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateReviewConditionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_RC_CON_001: C1=T, C2=T - 学生重复评论")
        @Transactional
        void testC1True_C2True() {
            // 准备 - 创建学生并评论
            RegisterRequest req = new RegisterRequest();
            req.setUsername("conduser001");
            req.setPassword("password123");
            req.setEmail("cond001@student.must.edu.mo");
            var user = userService.register(req);

            ReviewDto firstReview = new ReviewDto();
            firstReview.setUserId(user.getId());
            firstReview.setCourseId(testCourseId);
            firstReview.setRating(5);
            firstReview.setContent("第一次评论");
            var saved = reviewService.createReview(firstReview);

            // 测试 - C1=T (是学生), C2=T (已评论)
            ReviewDto secondReview = new ReviewDto();
            secondReview.setUserId(user.getId());
            secondReview.setCourseId(testCourseId);
            secondReview.setRating(4);
            secondReview.setContent("第二次评论");

            // 验证
            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.createReview(secondReview);
            });

            // 清理
            reviewRepository.deleteById(saved.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_RC_CON_002: C1=F, C2=F - 管理员首次评论")
        @Transactional
        void testC1False_C2False() {
            // 创建管理员用户
            User admin = new User();
            admin.setUsername("testadmin002");
            admin.setPassword("encoded");
            admin.setEmail("admin002@student.must.edu.mo");
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setActive(true);
            admin.setCanComment(true);
            admin = userRepository.save(admin);

            // 测试 - C1=F (是管理员), C2=F (未评论过)
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(admin.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("管理员评论");

            // 执行 - 管理员可以创建评论
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(admin.getId());
        }
    }

    /**
     * 1.4 条件组合覆盖测试 - 创建评论
     * 覆盖所有条件组合 (2^2 = 4种)
     */
    @Nested
    @DisplayName("1.4 创建评论 - 条件组合覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateReviewMultipleConditionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_RC_MCC_001: C1=T, C2=T - 学生重复评论")
        @Transactional
        void testC1True_C2True() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("mccuser001");
            req.setPassword("password123");
            req.setEmail("mcc001@student.must.edu.mo");
            var user = userService.register(req);

            ReviewDto first = new ReviewDto();
            first.setUserId(user.getId());
            first.setCourseId(testCourseId);
            first.setRating(5);
            first.setContent("第一次");
            var saved = reviewService.createReview(first);

            // 测试
            ReviewDto second = new ReviewDto();
            second.setUserId(user.getId());
            second.setCourseId(testCourseId);
            second.setRating(4);
            second.setContent("第二次");

            // 验证 - 应该抛出异常
            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.createReview(second);
            });

            // 清理
            reviewRepository.deleteById(saved.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_RC_MCC_002: C1=T, C2=F - 学生首次评论")
        @Transactional
        void testC1True_C2False() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("mccuser002");
            req.setPassword("password123");
            req.setEmail("mcc002@student.must.edu.mo");
            var user = userService.register(req);

            // 测试
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(4);
            reviewDto.setContent("学生首次评论");

            // 执行 - 应该成功
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_RC_MCC_003: C1=F, C2=T - 管理员重复评论（允许）")
        @Transactional
        void testC1False_C2True() {
            // 创建管理员
            User admin = new User();
            admin.setUsername("admin003");
            admin.setPassword("encoded");
            admin.setEmail("admin003@student.must.edu.mo");
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setActive(true);
            admin.setCanComment(true);
            admin = userRepository.save(admin);

            // 先发表一次评论
            ReviewDto first = new ReviewDto();
            first.setUserId(admin.getId());
            first.setCourseId(testCourseId);
            first.setRating(5);
            first.setContent("管理员第一次评论");
            var firstSaved = reviewService.createReview(first);

            // 测试 - 管理员再次评论同一课程（应该允许）
            ReviewDto second = new ReviewDto();
            second.setUserId(admin.getId());
            second.setCourseId(testCourseId);
            second.setRating(4);
            second.setContent("管理员第二次评论");

            // 执行 - 管理员可以重复评论
            ReviewDto result = reviewService.createReview(second);

            // 验证 - 应该成功
            assertNotNull(result);

            // 清理
            reviewRepository.deleteById(firstSaved.getId());
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_RC_MCC_004: C1=F, C2=F - 管理员首次评论")
        @Transactional
        void testC1False_C2False() {
            // 创建管理员
            User admin = new User();
            admin.setUsername("admin004");
            admin.setPassword("encoded");
            admin.setEmail("admin004@student.must.edu.mo");
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setActive(true);
            admin.setCanComment(true);
            admin = userRepository.save(admin);

            // 测试
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(admin.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("管理员评论");

            // 执行
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(admin.getId());
        }
    }

    /**
     * 1.5 基本路径测试 - 创建评论
     * 环路复杂度：V(G) = 4
     * 独立路径：
     * - Path 1: 用户不存在
     * - Path 2: 课程不存在
     * - Path 3: 非管理员已评论
     * - Path 4: 允许创建评论
     */
    @Nested
    @DisplayName("1.5 创建评论 - 基本路径测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateReviewBasicPathTests {

        @Test
        @Order(1)
        @DisplayName("TC_RC_BP_001: Path 1 - 用户不存在")
        @Transactional
        void testPath1_UserNotFound() {
            // 测试 - 使用不存在的用户ID
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(99999L);  // 不存在的用户ID
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("测试评论");

            // 验证 - 应该抛出用户不存在异常
            assertThrows(Exception.class, () -> {
                reviewService.createReview(reviewDto);
            });
        }

        @Test
        @Order(2)
        @DisplayName("TC_RC_BP_002: Path 2 - 课程不存在")
        @Transactional
        void testPath2_CourseNotFound() {
            // 准备 - 创建用户
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pathuser002");
            req.setPassword("password123");
            req.setEmail("path002@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 使用不存在的课程ID
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(99999L);  // 不存在的课程ID
            reviewDto.setRating(5);
            reviewDto.setContent("测试评论");

            // 验证 - 应该抛出课程不存在异常
            assertThrows(Exception.class, () -> {
                reviewService.createReview(reviewDto);
            });

            // 清理
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_RC_BP_003: Path 3 - 非管理员已评论")
        @Transactional
        void testPath3_StudentAlreadyReviewed() {
            // 准备 - 创建学生并发表评论
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pathuser003");
            req.setPassword("password123");
            req.setEmail("path003@student.must.edu.mo");
            var user = userService.register(req);

            ReviewDto first = new ReviewDto();
            first.setUserId(user.getId());
            first.setCourseId(testCourseId);
            first.setRating(5);
            first.setContent("第一次评论");
            var saved = reviewService.createReview(first);

            // 测试 - 再次评论
            ReviewDto second = new ReviewDto();
            second.setUserId(user.getId());
            second.setCourseId(testCourseId);
            second.setRating(4);
            second.setContent("第二次评论");

            // 验证 - 应该抛出已评论异常
            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.createReview(second);
            });

            // 清理
            reviewRepository.deleteById(saved.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_RC_BP_004: Path 4 - 允许创建评论（成功路径）")
        @Transactional
        void testPath4_CreateReviewSuccess() {
            // 准备 - 创建新学生
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pathuser004");
            req.setPassword("password123");
            req.setEmail("path004@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 首次评论
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setUserId(user.getId());
            reviewDto.setCourseId(testCourseId);
            reviewDto.setRating(5);
            reviewDto.setContent("非常好的课程！推荐给大家。");
            reviewDto.setAnonymous(false);

            // 执行
            ReviewDto result = reviewService.createReview(reviewDto);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(user.getId(), result.getUserId());
            assertEquals(testCourseId, result.getCourseId());
            assertEquals(5, result.getRating());
            assertTrue(result.getContent().contains("课程"));
            assertEquals(Review.ReviewStatus.APPROVED, result.getStatus());
            assertFalse(result.isPinned());  // 新评论默认不置顶

            // 清理
            reviewRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }
}


