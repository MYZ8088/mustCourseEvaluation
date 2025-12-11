package com.must.courseevaluation;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.service.CourseService;
import com.must.courseevaluation.service.CourseSummaryService;
import com.must.courseevaluation.service.ReviewService;
import com.must.courseevaluation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 集成测试 - 使用桩(Stubs)
 * 
 * 测试策略：自顶向下集成测试 (Top-Down Integration Testing)
 * 
 * 原理：
 * - 从最顶层的Controller开始测试
 * - 使用Stub（桩程序）替代下层的Service组件
 * - Stub模拟Service的行为，返回预定义的数据
 * - 验证Controller与Service之间的接口集成
 * 
 * 架构：
 * ┌─────────────────────────────────────┐
 * │     Controller Layer (被测试)       │
 * │  CourseController, ReviewController │
 * └──────────────┬──────────────────────┘
 *                │ 调用
 *                ▼
 * ┌─────────────────────────────────────┐
 * │     Service Layer (Stub 桩程序)     │
 * │   @MockBean 模拟的Service组件       │
 * │   返回预定义数据，不访问真实数据库    │
 * └─────────────────────────────────────┘
 * 
 * 优点：
 * 1. 可以在底层模块未完成时测试顶层模块
 * 2. 测试速度快（不需要真实数据库操作）
 * 3. 可以模拟各种边界情况和异常场景
 * 
 * @author Course Evaluation System
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("集成测试 - 使用Stubs（桩程序）")
public class IntegrationTestWithStubs {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper available for JSON operations if needed

    /**
     * ==================== STUB定义 ====================
     * 使用@MockBean创建Service层的桩程序
     * 这些Stub会替代真实的Service实现
     */
    
    @MockBean
    private CourseService courseServiceStub;  // 课程服务桩
    
    @MockBean
    private ReviewService reviewServiceStub;  // 评论服务桩
    
    @MockBean
    private UserService userServiceStub;      // 用户服务桩
    
    @MockBean
    private CourseSummaryService courseSummaryServiceStub;  // 课程总结服务桩

    /**
     * ==================== 测试数据准备 ====================
     */
    
    private CourseDto createMockCourseDto(Long id, String name, String code) {
        CourseDto dto = new CourseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setCredits(3.0);
        dto.setDescription("测试课程描述");
        dto.setType("COMPULSORY");
        dto.setFacultyId(1L);
        dto.setFacultyName("信息学院");
        dto.setTeacherId(1L);
        dto.setTeacherName("测试教师");
        return dto;
    }
    
    private ReviewDto createMockReviewDto(Long id, Long userId, Long courseId, int rating) {
        ReviewDto dto = new ReviewDto();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setCourseId(courseId);
        dto.setRating(rating);
        dto.setContent("这是一条测试评论内容");
        dto.setAnonymous(false);
        dto.setStatus(Review.ReviewStatus.APPROVED);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
    
    private UserDto createMockUserDto(Long id, String username) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(username + "@student.must.edu.mo");
        dto.setRole(User.Role.ROLE_STUDENT);
        dto.setActive(true);
        dto.setCanComment(true);
        return dto;
    }

    /**
     * ==================== 1. CourseController + Stub 集成测试 ====================
     * 
     * 测试场景：Controller调用Service Stub获取课程数据
     */
    @Nested
    @DisplayName("1. CourseController + CourseService Stub 集成测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseControllerStubIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("IT_STUB_001: 获取所有课程 - Stub返回课程列表")
        void testGetAllCourses_StubReturnsListOfCourses() throws Exception {
            // ========== 配置Stub行为 ==========
            List<CourseDto> mockCourses = Arrays.asList(
                createMockCourseDto(1L, "软件工程", "SE101"),
                createMockCourseDto(2L, "数据库原理", "DB201"),
                createMockCourseDto(3L, "操作系统", "OS301")
            );
            
            // 当调用getAllCourses时，Stub返回预定义的课程列表
            when(courseServiceStub.getAllCourses()).thenReturn(mockCourses);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses")
                    .contentType(MediaType.APPLICATION_JSON))
                    // ========== 验证响应 ==========
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].name").value("软件工程"))
                    .andExpect(jsonPath("$[0].code").value("SE101"))
                    .andExpect(jsonPath("$[1].name").value("数据库原理"))
                    .andExpect(jsonPath("$[2].name").value("操作系统"));
            
            // ========== 验证Stub被调用 ==========
            verify(courseServiceStub, times(1)).getAllCourses();
        }

        @Test
        @Order(2)
        @DisplayName("IT_STUB_002: 获取单个课程 - Stub返回指定课程")
        void testGetCourseById_StubReturnsSingleCourse() throws Exception {
            // ========== 配置Stub行为 ==========
            CourseDto mockCourse = createMockCourseDto(1L, "软件工程", "SE101");
            
            when(courseServiceStub.getCourseById(1L)).thenReturn(mockCourse);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    // ========== 验证响应 ==========
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("软件工程"))
                    .andExpect(jsonPath("$.code").value("SE101"))
                    .andExpect(jsonPath("$.credits").value(3.0));
            
            // ========== 验证Stub被正确调用 ==========
            verify(courseServiceStub, times(1)).getCourseById(1L);
        }

        @Test
        @Order(3)
        @DisplayName("IT_STUB_003: 按院系获取课程 - Stub返回筛选结果")
        void testGetCoursesByFaculty_StubReturnsFilteredCourses() throws Exception {
            // ========== 配置Stub行为 ==========
            List<CourseDto> mockCourses = Arrays.asList(
                createMockCourseDto(1L, "软件工程", "SE101"),
                createMockCourseDto(2L, "计算机网络", "CN201")
            );
            
            when(courseServiceStub.getCoursesByFaculty(1L)).thenReturn(mockCourses);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses/faculty/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
            
            verify(courseServiceStub, times(1)).getCoursesByFaculty(1L);
        }

        @Test
        @Order(4)
        @DisplayName("IT_STUB_004: 搜索课程 - Stub返回搜索结果")
        void testSearchCourses_StubReturnsSearchResults() throws Exception {
            // ========== 配置Stub行为 ==========
            List<CourseDto> mockResults = Collections.singletonList(
                createMockCourseDto(1L, "软件工程", "SE101")
            );
            
            when(courseServiceStub.searchCourses("软件")).thenReturn(mockResults);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "软件")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("软件工程"));
            
            verify(courseServiceStub, times(1)).searchCourses("软件");
        }

        @Test
        @Order(5)
        @DisplayName("IT_STUB_005: Stub返回空列表 - 验证空结果处理")
        void testGetAllCourses_StubReturnsEmptyList() throws Exception {
            // ========== 配置Stub返回空列表 ==========
            when(courseServiceStub.getAllCourses()).thenReturn(Collections.emptyList());
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
            
            verify(courseServiceStub, times(1)).getAllCourses();
        }
    }

    /**
     * ==================== 2. ReviewController + Stub 集成测试 ====================
     * 
     * 测试场景：Controller调用Service Stub获取评论数据
     */
    @Nested
    @DisplayName("2. ReviewController + ReviewService Stub 集成测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewControllerStubIntegrationTests {

        @Test
        @Order(1)
        @WithMockUser(username = "student", roles = {"STUDENT"})
        @DisplayName("IT_STUB_006: 获取课程评论 - Stub返回评论列表")
        void testGetReviewsByCourse_StubReturnsReviewList() throws Exception {
            // ========== 配置Stub行为 ==========
            List<ReviewDto> mockReviews = Arrays.asList(
                createMockReviewDto(1L, 1L, 1L, 5),
                createMockReviewDto(2L, 2L, 1L, 4),
                createMockReviewDto(3L, 3L, 1L, 3)
            );
            
            when(reviewServiceStub.getReviewsByCourse(1L)).thenReturn(mockReviews);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/reviews/course/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].rating").value(5))
                    .andExpect(jsonPath("$[1].rating").value(4));
            
            verify(reviewServiceStub, times(1)).getReviewsByCourse(1L);
        }

        @Test
        @Order(2)
        @WithMockUser(username = "student", roles = {"STUDENT"})
        @DisplayName("IT_STUB_007: 获取单个评论 - Stub返回指定评论")
        void testGetReviewById_StubReturnsSingleReview() throws Exception {
            // ========== 配置Stub行为 ==========
            ReviewDto mockReview = createMockReviewDto(1L, 1L, 1L, 5);
            
            when(reviewServiceStub.getReviewById(1L)).thenReturn(mockReview);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/reviews/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.content").value("这是一条测试评论内容"));
            
            verify(reviewServiceStub, times(1)).getReviewById(1L);
        }

        @Test
        @Order(3)
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("IT_STUB_008: 管理员获取所有评论 - Stub返回完整列表")
        void testGetAllReviews_AdminRole_StubReturnsAllReviews() throws Exception {
            // ========== 配置Stub行为 ==========
            List<ReviewDto> mockReviews = Arrays.asList(
                createMockReviewDto(1L, 1L, 1L, 5),
                createMockReviewDto(2L, 2L, 2L, 4)
            );
            
            when(reviewServiceStub.getAllReviews()).thenReturn(mockReviews);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/reviews")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
            
            verify(reviewServiceStub, times(1)).getAllReviews();
        }

        @Test
        @Order(4)
        @WithMockUser(username = "student", roles = {"STUDENT"})
        @DisplayName("IT_STUB_009: 获取教师评论 - Stub返回教师相关评论")
        void testGetReviewsByTeacher_StubReturnsTeacherReviews() throws Exception {
            // ========== 配置Stub行为 ==========
            List<ReviewDto> mockReviews = Arrays.asList(
                createMockReviewDto(1L, 1L, 1L, 5),
                createMockReviewDto(2L, 2L, 2L, 4)
            );
            
            when(reviewServiceStub.getReviewsByTeacher(1L)).thenReturn(mockReviews);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/reviews/teacher/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
            
            verify(reviewServiceStub, times(1)).getReviewsByTeacher(1L);
        }
    }

    /**
     * ==================== 3. UserController + Stub 集成测试 ====================
     * 
     * 测试场景：Controller调用Service Stub获取用户数据
     */
    @Nested
    @DisplayName("3. UserController + UserService Stub 集成测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UserControllerStubIntegrationTests {

        @Test
        @Order(1)
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("IT_STUB_010: 管理员获取所有用户 - Stub返回用户列表")
        void testGetAllUsers_AdminRole_StubReturnsUserList() throws Exception {
            // ========== 配置Stub行为 ==========
            List<UserDto> mockUsers = Arrays.asList(
                createMockUserDto(1L, "student1"),
                createMockUserDto(2L, "student2"),
                createMockUserDto(3L, "student3")
            );
            
            when(userServiceStub.findAll()).thenReturn(mockUsers);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/users")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].username").value("student1"))
                    .andExpect(jsonPath("$[1].username").value("student2"));
            
            verify(userServiceStub, times(1)).findAll();
        }

        @Test
        @Order(2)
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("IT_STUB_011: 获取单个用户 - Stub返回指定用户")
        void testGetUserById_StubReturnsSingleUser() throws Exception {
            // ========== 配置Stub行为 ==========
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("testuser");
            mockUser.setEmail("testuser@student.must.edu.mo");
            mockUser.setRole(User.Role.ROLE_STUDENT);
            mockUser.setActive(true);
            
            when(userServiceStub.findById(1L)).thenReturn(mockUser);
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("testuser@student.must.edu.mo"));
            
            verify(userServiceStub, times(1)).findById(1L);
        }
    }

    /**
     * ==================== 4. 异常场景 Stub 测试 ====================
     * 
     * 测试场景：Stub模拟异常情况
     */
    @Nested
    @DisplayName("4. 异常场景 - Stub模拟异常")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ExceptionStubIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("IT_STUB_012: Stub模拟资源不存在异常")
        void testResourceNotFound_StubThrowsException() throws Exception {
            // ========== 配置Stub抛出异常 ==========
            when(courseServiceStub.getCourseById(999L))
                .thenThrow(new com.must.courseevaluation.exception.ResourceNotFoundException("课程不存在，ID: 999"));
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
            
            verify(courseServiceStub, times(1)).getCourseById(999L);
        }

        @Test
        @Order(2)
        @DisplayName("IT_STUB_013: Stub模拟服务内部错误")
        void testInternalError_StubThrowsRuntimeException() throws Exception {
            // ========== 配置Stub抛出运行时异常 ==========
            when(courseServiceStub.getAllCourses())
                .thenThrow(new RuntimeException("数据库连接失败"));
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
            
            verify(courseServiceStub, times(1)).getAllCourses();
        }
    }

    /**
     * ==================== 5. Stub行为验证测试 ====================
     * 
     * 测试场景：验证Controller正确调用Service方法
     */
    @Nested
    @DisplayName("5. Stub行为验证 - 验证方法调用")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StubBehaviorVerificationTests {

        @Test
        @Order(1)
        @DisplayName("IT_STUB_014: 验证Stub方法调用次数")
        void testVerifyStubMethodCallCount() throws Exception {
            // ========== 配置Stub行为 ==========
            when(courseServiceStub.getAllCourses()).thenReturn(Collections.emptyList());
            
            // ========== 多次调用 ==========
            mockMvc.perform(get("/courses")).andExpect(status().isOk());
            mockMvc.perform(get("/courses")).andExpect(status().isOk());
            mockMvc.perform(get("/courses")).andExpect(status().isOk());
            
            // ========== 验证调用次数 ==========
            verify(courseServiceStub, times(3)).getAllCourses();
        }

        @Test
        @Order(2)
        @DisplayName("IT_STUB_015: 验证Stub方法调用参数")
        void testVerifyStubMethodArguments() throws Exception {
            // ========== 配置Stub行为 ==========
            when(courseServiceStub.searchCourses(anyString())).thenReturn(Collections.emptyList());
            
            // ========== 执行测试 ==========
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据库"))
                    .andExpect(status().isOk());
            
            // ========== 验证参数 ==========
            verify(courseServiceStub).searchCourses("数据库");
        }

        @Test
        @Order(3)
        @DisplayName("IT_STUB_016: 验证Stub方法从未被调用")
        void testVerifyStubMethodNeverCalled() throws Exception {
            // ========== 配置Stub行为 ==========
            when(courseServiceStub.getCourseById(anyLong()))
                .thenReturn(createMockCourseDto(1L, "测试课程", "TEST001"));
            
            // ========== 只调用一次 ==========
            mockMvc.perform(get("/courses/1")).andExpect(status().isOk());
            
            // ========== 验证其他方法从未被调用 ==========
            verify(courseServiceStub, never()).getAllCourses();
            verify(courseServiceStub, never()).searchCourses(anyString());
            verify(courseServiceStub, times(1)).getCourseById(1L);
        }
    }
}

