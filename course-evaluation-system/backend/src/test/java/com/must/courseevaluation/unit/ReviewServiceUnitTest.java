package com.must.courseevaluation.unit;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.*;
import com.must.courseevaluation.repository.*;
import com.must.courseevaluation.service.ContentFilterService;
import com.must.courseevaluation.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewService 单元测试
 * 使用 Mockito 模拟依赖，测试 ReviewServiceImpl 的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 单元测试")
class ReviewServiceUnitTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ContentFilterService contentFilterService;

    @Mock
    private ReviewVoteRepository reviewVoteRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Course testCourse;
    private Review testReview;
    private ReviewDto testReviewDto;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        // 初始化测试院系
        testFaculty = new Faculty();
        testFaculty.setId(1L);
        testFaculty.setName("计算机学院");

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@student.must.edu.mo");
        testUser.setRole(User.Role.ROLE_STUDENT);

        // 初始化测试课程
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("CS101");
        testCourse.setName("计算机导论");
        testCourse.setFaculty(testFaculty);

        // 初始化测试评价
        testReview = new Review();
        testReview.setId(1L);
        testReview.setContent("这是一门很好的课程");
        testReview.setRating(5);
        testReview.setAnonymous(false);
        testReview.setUser(testUser);
        testReview.setCourse(testCourse);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setStatus(Review.ReviewStatus.APPROVED);
        testReview.setPinned(false);

        // 初始化测试DTO
        testReviewDto = new ReviewDto();
        testReviewDto.setId(1L);
        testReviewDto.setContent("这是一门很好的课程");
        testReviewDto.setRating(5);
        testReviewDto.setAnonymous(false);
        testReviewDto.setUserId(1L);
        testReviewDto.setCourseId(1L);
    }

    // ==================== getAllReviews() 测试 ====================

    @Nested
    @DisplayName("getAllReviews() 方法测试")
    class GetAllReviewsTests {

        @Test
        @DisplayName("返回所有评价列表")
        void testGetAllReviewsSuccess() {
            // Given
            Review review2 = new Review();
            review2.setId(2L);
            review2.setContent("另一个评价");
            review2.setRating(4);
            review2.setUser(testUser);
            review2.setCourse(testCourse);
            review2.setCreatedAt(LocalDateTime.now());
            review2.setStatus(Review.ReviewStatus.APPROVED);

            when(reviewRepository.findAll()).thenReturn(Arrays.asList(testReview, review2));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            List<ReviewDto> result = reviewService.getAllReviews();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(reviewRepository).findAll();
        }

        @Test
        @DisplayName("无评价 - 返回空列表")
        void testGetAllReviewsEmpty() {
            // Given
            when(reviewRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<ReviewDto> result = reviewService.getAllReviews();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(reviewRepository).findAll();
        }
    }

    // ==================== getReviewById() 测试 ====================

    @Nested
    @DisplayName("getReviewById() 方法测试")
    class GetReviewByIdTests {

        @Test
        @DisplayName("成功获取评价")
        void testGetReviewByIdSuccess() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.getReviewById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("这是一门很好的课程", result.getContent());
            assertEquals(5, result.getRating());
            verify(reviewRepository).findById(1L);
        }

        @Test
        @DisplayName("评价不存在 - 抛出异常")
        void testGetReviewByIdNotFound() {
            // Given
            when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.getReviewById(999L));
            verify(reviewRepository).findById(999L);
        }
    }

    // ==================== getReviewsByCourse() 测试 ====================

    @Nested
    @DisplayName("getReviewsByCourse() 方法测试")
    class GetReviewsByCourseTests {

        @Test
        @DisplayName("成功获取课程评价列表")
        void testGetReviewsByCourseSuccess() {
            // Given
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByCourse(testCourse)).thenReturn(Arrays.asList(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            List<ReviewDto> result = reviewService.getReviewsByCourse(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(courseRepository).findById(1L);
            verify(reviewRepository).findByCourse(testCourse);
        }

        @Test
        @DisplayName("评价排序 - 置顶评价优先")
        void testGetReviewsByCourseWithPinnedFirst() {
            // Given
            Review pinnedReview = new Review();
            pinnedReview.setId(2L);
            pinnedReview.setContent("置顶评价");
            pinnedReview.setRating(5);
            pinnedReview.setUser(testUser);
            pinnedReview.setCourse(testCourse);
            pinnedReview.setCreatedAt(LocalDateTime.now().minusDays(1));
            pinnedReview.setStatus(Review.ReviewStatus.APPROVED);
            pinnedReview.setPinned(true);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByCourse(testCourse)).thenReturn(Arrays.asList(testReview, pinnedReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            List<ReviewDto> result = reviewService.getReviewsByCourse(1L);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.get(0).isPinned()); // 置顶评价应该排在前面
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testGetReviewsByCourseNotFound() {
            // Given
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.getReviewsByCourse(999L));
            verify(courseRepository).findById(999L);
            verify(reviewRepository, never()).findByCourse(any());
        }
    }

    // ==================== getReviewsByUser() 测试 ====================

    @Nested
    @DisplayName("getReviewsByUser() 方法测试")
    class GetReviewsByUserTests {

        @Test
        @DisplayName("成功获取用户评价列表")
        void testGetReviewsByUserSuccess() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByUser(testUser)).thenReturn(Arrays.asList(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            List<ReviewDto> result = reviewService.getReviewsByUser(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findById(1L);
            verify(reviewRepository).findByUser(testUser);
        }

        @Test
        @DisplayName("用户不存在 - 抛出异常")
        void testGetReviewsByUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.getReviewsByUser(999L));
            verify(userRepository).findById(999L);
        }
    }

    // ==================== createReview() 测试 ====================

    @Nested
    @DisplayName("createReview() 方法测试")
    class CreateReviewTests {

        @Test
        @DisplayName("成功创建评价")
        void testCreateReviewSuccess() {
            // Given
            ReviewDto newReviewDto = new ReviewDto();
            newReviewDto.setContent("新的评价内容");
            newReviewDto.setRating(4);
            newReviewDto.setAnonymous(false);
            newReviewDto.setUserId(1L);
            newReviewDto.setCourseId(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.existsByUserAndCourse(testUser, testCourse)).thenReturn(false);
            when(contentFilterService.filterContent("新的评价内容")).thenReturn("新的评价内容");
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review review = invocation.getArgument(0);
                review.setId(2L);
                return review;
            });
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.createReview(newReviewDto);

            // Then
            assertNotNull(result);
            assertEquals("新的评价内容", result.getContent());
            assertEquals(4, result.getRating());
            verify(contentFilterService).filterContent("新的评价内容");
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("用户不存在 - 抛出异常")
        void testCreateReviewUserNotFound() {
            // Given
            testReviewDto.setUserId(999L);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.createReview(testReviewDto));
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testCreateReviewCourseNotFound() {
            // Given
            testReviewDto.setCourseId(999L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.createReview(testReviewDto));
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("重复评价 - 抛出异常")
        void testCreateReviewDuplicate() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.existsByUserAndCourse(testUser, testCourse)).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(testReviewDto));
            
            assertTrue(exception.getMessage().contains("已经对这门课程发表过评价"));
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("敏感词过滤")
        void testCreateReviewWithSensitiveContent() {
            // Given
            ReviewDto newReviewDto = new ReviewDto();
            newReviewDto.setContent("这门课很傻逼");
            newReviewDto.setRating(1);
            newReviewDto.setAnonymous(false);
            newReviewDto.setUserId(1L);
            newReviewDto.setCourseId(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.existsByUserAndCourse(testUser, testCourse)).thenReturn(false);
            when(contentFilterService.filterContent("这门课很傻逼")).thenReturn("这门课很**");
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review review = invocation.getArgument(0);
                review.setId(2L);
                return review;
            });
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.createReview(newReviewDto);

            // Then
            assertNotNull(result);
            verify(contentFilterService).filterContent("这门课很傻逼");
        }
    }

    // ==================== updateReview() 测试 ====================

    @Nested
    @DisplayName("updateReview() 方法测试")
    class UpdateReviewTests {

        @Test
        @DisplayName("成功更新评价")
        void testUpdateReviewSuccess() {
            // Given
            testReviewDto.setContent("更新后的评价内容");
            testReviewDto.setRating(4);
            
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(contentFilterService.filterContent("更新后的评价内容")).thenReturn("更新后的评价内容");
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.updateReview(testReviewDto);

            // Then
            assertNotNull(result);
            verify(reviewRepository).findById(1L);
            verify(contentFilterService).filterContent("更新后的评价内容");
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("评价不存在 - 抛出异常")
        void testUpdateReviewNotFound() {
            // Given
            testReviewDto.setId(999L);
            when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.updateReview(testReviewDto));
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("无权限修改 - 抛出异常")
        void testUpdateReviewNoPermission() {
            // Given
            testReviewDto.setUserId(999L); // 不是评价的作者
            
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> reviewService.updateReview(testReviewDto));
            
            assertTrue(exception.getMessage().contains("无权修改此评价"));
            verify(reviewRepository, never()).save(any(Review.class));
        }
    }

    // ==================== deleteReview() 测试 ====================

    @Nested
    @DisplayName("deleteReview() 方法测试")
    class DeleteReviewTests {

        @Test
        @DisplayName("成功删除评价")
        void testDeleteReviewSuccess() {
            // Given
            when(reviewRepository.existsById(1L)).thenReturn(true);
            doNothing().when(reviewRepository).deleteById(1L);

            // When
            reviewService.deleteReview(1L);

            // Then
            verify(reviewRepository).existsById(1L);
            verify(reviewRepository).deleteById(1L);
        }

        @Test
        @DisplayName("评价不存在 - 抛出异常")
        void testDeleteReviewNotFound() {
            // Given
            when(reviewRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.deleteReview(999L));
            verify(reviewRepository).existsById(999L);
            verify(reviewRepository, never()).deleteById(anyLong());
        }
    }

    // ==================== voteReview() 测试 ====================

    @Nested
    @DisplayName("voteReview() 方法测试")
    class VoteReviewTests {

        @Test
        @DisplayName("成功点赞评价")
        void testVoteReviewLikeSuccess() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewVoteRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
            when(reviewVoteRepository.save(any(ReviewVote.class))).thenAnswer(invocation -> {
                ReviewVote vote = invocation.getArgument(0);
                vote.setId(1L);
                return vote;
            });
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(1L);

            // When
            ReviewDto result = reviewService.voteReview(1L, 1L, "LIKE");

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).save(any(ReviewVote.class));
        }

        @Test
        @DisplayName("取消已有点赞")
        void testVoteReviewCancelLike() {
            // Given
            ReviewVote existingVote = new ReviewVote();
            existingVote.setId(1L);
            existingVote.setReview(testReview);
            existingVote.setUser(testUser);
            existingVote.setVoteType(ReviewVote.VoteType.LIKE);

            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewVoteRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingVote));
            doNothing().when(reviewVoteRepository).delete(existingVote);
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.voteReview(1L, 1L, "LIKE");

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).delete(existingVote);
        }

        @Test
        @DisplayName("切换投票类型")
        void testVoteReviewSwitchType() {
            // Given
            ReviewVote existingVote = new ReviewVote();
            existingVote.setId(1L);
            existingVote.setReview(testReview);
            existingVote.setUser(testUser);
            existingVote.setVoteType(ReviewVote.VoteType.LIKE);

            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewVoteRepository.findByReviewIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingVote));
            when(reviewVoteRepository.save(any(ReviewVote.class))).thenReturn(existingVote);
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.voteReview(1L, 1L, "DISLIKE");

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).save(any(ReviewVote.class));
            verify(reviewVoteRepository, never()).delete(any(ReviewVote.class));
        }

        @Test
        @DisplayName("无效投票类型 - 抛出异常")
        void testVoteReviewInvalidType() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> reviewService.voteReview(1L, 1L, "INVALID"));
            
            assertTrue(exception.getMessage().contains("无效的投票类型"));
        }
    }

    // ==================== getCourseRatings() 测试 ====================

    @Nested
    @DisplayName("getCourseRatings() 方法测试")
    class GetCourseRatingsTests {

        @Test
        @DisplayName("计算课程评分统计")
        void testGetCourseRatingsSuccess() {
            // Given
            Review review2 = new Review();
            review2.setId(2L);
            review2.setRating(4);
            review2.setUser(testUser);
            review2.setCourse(testCourse);
            review2.setStatus(Review.ReviewStatus.APPROVED);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByCourse(testCourse)).thenReturn(Arrays.asList(testReview, review2));

            // When
            Map<String, Object> result = reviewService.getCourseRatings(1L);

            // Then
            assertNotNull(result);
            assertEquals(2, result.get("totalReviews"));
            assertNotNull(result.get("averageRating"));
            assertNotNull(result.get("ratingDistribution"));
            verify(courseRepository).findById(1L);
        }

        @Test
        @DisplayName("无评价 - 返回默认统计")
        void testGetCourseRatingsEmpty() {
            // Given
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByCourse(testCourse)).thenReturn(Collections.emptyList());

            // When
            Map<String, Object> result = reviewService.getCourseRatings(1L);

            // Then
            assertNotNull(result);
            assertEquals(0, result.get("totalReviews"));
            assertEquals(0.0, result.get("averageRating"));
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testGetCourseRatingsNotFound() {
            // Given
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> reviewService.getCourseRatings(999L));
        }
    }

    // ==================== 匿名评价测试 ====================

    @Nested
    @DisplayName("匿名评价测试")
    class AnonymousReviewTests {

        @Test
        @DisplayName("匿名评价 - 用户名显示为匿名用户")
        void testAnonymousReviewUsername() {
            // Given
            testReview.setAnonymous(true);
            
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.getReviewById(1L);

            // Then
            assertNotNull(result);
            assertEquals("匿名用户", result.getUsername());
            assertTrue(result.isAnonymous());
        }

        @Test
        @DisplayName("非匿名评价 - 显示真实用户名")
        void testNonAnonymousReviewUsername() {
            // Given
            testReview.setAnonymous(false);
            
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.getReviewById(1L);

            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertFalse(result.isAnonymous());
        }
    }

    // ==================== getUserReviewForCourse() 测试 ====================

    @Nested
    @DisplayName("getUserReviewForCourse() 方法测试")
    class GetUserReviewForCourseTests {

        @Test
        @DisplayName("成功获取用户对课程的评价")
        void testGetUserReviewForCourseSuccess() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.of(testReview));
            when(reviewVoteRepository.countByReviewIdAndVoteType(anyLong(), any())).thenReturn(0L);

            // When
            ReviewDto result = reviewService.getUserReviewForCourse(1L, 1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(reviewRepository).findByUserAndCourse(testUser, testCourse);
        }

        @Test
        @DisplayName("用户未评价该课程 - 返回null")
        void testGetUserReviewForCourseNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.empty());

            // When
            ReviewDto result = reviewService.getUserReviewForCourse(1L, 1L);

            // Then
            assertNull(result);
        }
    }
}





