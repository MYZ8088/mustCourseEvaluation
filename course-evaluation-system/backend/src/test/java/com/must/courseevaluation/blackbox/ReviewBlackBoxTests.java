package com.must.courseevaluation.blackbox;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.TeacherRepository;
import com.must.courseevaluation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 评论模块黑盒测试
 * 
 * 测试功能：创建评论
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
@DisplayName("评论模块 - 黑盒测试")
public class ReviewBlackBoxTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_STUDENT_USERNAME = "review_test_student";
    private static final String TEST_ADMIN_USERNAME = "review_test_admin";
    private Long testCourseId;
    private Long testTeacherId;

    @BeforeAll
    @Transactional
    void setupTestData() {
        // 创建或查找测试学院
        Faculty testFaculty = facultyRepository.findAll().stream()
                .filter(f -> "测试学院_Review".equals(f.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Faculty newFaculty = new Faculty();
                    newFaculty.setName("测试学院_Review");
                    newFaculty.setDescription("用于Review测试的学院");
                    return facultyRepository.save(newFaculty);
                });

        // 创建或查找测试教师
        Teacher testTeacher = teacherRepository.findAll().stream()
                .filter(t -> "测试教师_Review".equals(t.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Teacher newTeacher = new Teacher();
                    newTeacher.setName("测试教师_Review");
                    newTeacher.setTitle("教授");
                    return teacherRepository.save(newTeacher);
                });
        testTeacherId = testTeacher.getId();

        // 创建或查找测试课程
        Course testCourse = courseRepository.findAll().stream()
                .filter(c -> "TEST_REV_101".equals(c.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    Course newCourse = new Course();
                    newCourse.setCode("TEST_REV_101");
                    newCourse.setName("测试课程_Review");
                    newCourse.setCredits(3.0);
                    newCourse.setFaculty(testFaculty);
                    newCourse.setTeacher(testTeacher);
                    return courseRepository.save(newCourse);
                });
        testCourseId = testCourse.getId();

        // 创建测试学生用户
        userRepository.findByUsername(TEST_STUDENT_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );
        User testStudent = new User();
        testStudent.setUsername(TEST_STUDENT_USERNAME);
        testStudent.setPassword(passwordEncoder.encode("password123"));
        testStudent.setEmail("review_test_student@student.must.edu.mo");
        testStudent.setRole(User.Role.ROLE_STUDENT);
        testStudent.setActive(true);
        testStudent.setCanComment(true);
        userRepository.save(testStudent);

        // 创建测试管理员用户
        userRepository.findByUsername(TEST_ADMIN_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );
        User testAdmin = new User();
        testAdmin.setUsername(TEST_ADMIN_USERNAME);
        testAdmin.setPassword(passwordEncoder.encode("password123"));
        testAdmin.setEmail("review_test_admin@must.edu.mo");
        testAdmin.setRole(User.Role.ROLE_ADMIN);
        testAdmin.setActive(true);
        testAdmin.setCanComment(true);
        userRepository.save(testAdmin);
    }

    @AfterAll
    @Transactional
    void cleanupTestData() {
        // 清理测试用户
        userRepository.findByUsername(TEST_STUDENT_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );
        userRepository.findByUsername(TEST_ADMIN_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );
    }

    /**
     * 2.1 等价类划分测试 - 创建评论
     */
    @Nested
    @DisplayName("2.1 创建评论 - 等价类划分测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewEquivalencePartitioningTests {

        @Test
        @Order(1)
        @DisplayName("TC_REV_EQ_001: 正常评论（学生） - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testCreateReviewAsStudent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            reviewDto.setAnonymous(false);

            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(2)
        @DisplayName("TC_REV_EQ_002: 正常评论（管理员） - 成功")
        @WithUserDetails(TEST_ADMIN_USERNAME)
        @Transactional
        void testCreateReviewAsAdmin() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(4);
            reviewDto.setContent("推荐课程");
            reviewDto.setCourseId(testCourseId);
            reviewDto.setAnonymous(false);

            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REV_EQ_003: 评分为空 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithNullRating() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(null);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REV_EQ_004: 评分为0 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithRating0() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(0);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(5)
        @DisplayName("TC_REV_EQ_005: 评分为负数 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithNegativeRating() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(-1);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(6)
        @DisplayName("TC_REV_EQ_006: 评分超过5 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithRatingOver5() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(6);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        @DisplayName("TC_REV_EQ_008: 内容为空 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithEmptyContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_REV_EQ_010: 内容仅空白 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testCreateReviewWithBlankContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("   ");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(9)
        @DisplayName("TC_REV_EQ_011: 未登录 - 失败")
        void testCreateReviewWithoutAuthentication() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 2.2 边界值测试 - 创建评论
     */
    @Nested
    @DisplayName("2.2 创建评论 - 边界值测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewBoundaryValueTests {

        @Test
        @Order(1)
        @DisplayName("TC_REV_BV_001: 评分=0 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testRating0() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(0);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        @DisplayName("TC_REV_BV_002: 评分=1 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRating1() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(1);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REV_BV_003: 评分=2 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRating2() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(2);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REV_BV_004: 评分=3 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRating3() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(3);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(5)
        @DisplayName("TC_REV_BV_005: 评分=4 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRating4() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(4);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(6)
        @DisplayName("TC_REV_BV_006: 评分=5 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRating5() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(7)
        @DisplayName("TC_REV_BV_007: 评分=6 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testRating6() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(6);
            reviewDto.setContent("课程不错");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_REV_BV_008: 内容为空 - 失败")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testEmptyContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(9)
        @DisplayName("TC_REV_BV_009: 内容1字符 - 成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testOneCharContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("好");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }
    }

    /**
     * 2.3 因果图测试 - 创建评论
     */
    @Nested
    @DisplayName("2.3 创建评论 - 因果图测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewCauseEffectTests {

        @Test
        @Order(1)
        @DisplayName("TC_REV_CE_001: C1∧C2∧C3∧C4∧C5 = 1 - 创建成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testAllConditionsTrue() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(2)
        @DisplayName("TC_REV_CE_002: C1=0 (未登录) - 返回401")
        void testNotLoggedIn() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REV_CE_005: C4=0 (评分无效) - 返回400")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testInvalidRating() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(6); // 超出范围
            reviewDto.setContent("很好的课程");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REV_CE_006: C5=0 (内容为空) - 返回400")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testEmptyContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 2.4 判定表测试 - 创建评论
     */
    @Nested
    @DisplayName("2.4 创建评论 - 判定表测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewDecisionTableTests {

        @Test
        @Order(1)
        @DisplayName("TC_REV_DT_001: R1 所有条件满足 - 创建成功")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        @Transactional
        void testRule1_AllConditionsMet() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(2)
        @DisplayName("TC_REV_DT_002: R2 未登录 - 返回401")
        void testRule2_NotLoggedIn() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("很好");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(3)
        @DisplayName("TC_REV_DT_005: R5 评分无效 - 返回400")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testRule5_InvalidRating() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(6); // 超出范围
            reviewDto.setContent("很好");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_REV_DT_006: R6 内容为空 - 返回400")
        @WithUserDetails(TEST_STUDENT_USERNAME)
        void testRule6_EmptyContent() throws Exception {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setRating(5);
            reviewDto.setContent("");
            reviewDto.setCourseId(testCourseId);
            mockMvc.perform(post("/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isBadRequest());
        }
    }
}


