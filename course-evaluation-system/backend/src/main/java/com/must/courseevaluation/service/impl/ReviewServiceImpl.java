package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.ReviewVote;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import com.must.courseevaluation.repository.ReviewVoteRepository;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.ContentFilterService;
import com.must.courseevaluation.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ContentFilterService contentFilterService;
    
    @Autowired
    private ReviewVoteRepository reviewVoteRepository;
    
    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + id));
        return convertToDto(review);
    }
    
    @Override
    public List<ReviewDto> getReviewsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
                
        List<Review> reviews = reviewRepository.findByCourse(course);
        
        // 对评论进行排序，置顶的评论排在前面，然后按创建时间降序排序
        reviews.sort((r1, r2) -> {
            if (r1.isPinned() && !r2.isPinned()) {
                return -1;
            } else if (!r1.isPinned() && r2.isPinned()) {
                return 1;
            } else {
                return r2.getCreatedAt().compareTo(r1.getCreatedAt()); // 先创建的排后面
            }
        });
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getReviewsByTeacher(Long teacherId) {
        List<Review> reviews = reviewRepository.findByTeacherId(teacherId);
        
        // 对评论进行排序，置顶的评论排在前面，然后按创建时间降序排序
        reviews.sort((r1, r2) -> {
            if (r1.isPinned() && !r2.isPinned()) {
                return -1;
            } else if (!r1.isPinned() && r2.isPinned()) {
                return 1;
            } else {
                return r2.getCreatedAt().compareTo(r1.getCreatedAt()); // 先创建的排后面
            }
        });
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
                
        return reviewRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + reviewDto.getUserId()));
                
        Course course = courseRepository.findById(reviewDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + reviewDto.getCourseId()));
        
        // 检查用户是否已经对该课程发表过评价 - 非管理员的情况下
        if (user.getRole() != User.Role.ROLE_ADMIN && reviewRepository.existsByUserAndCourse(user, course)) {
            throw new IllegalArgumentException("您已经对这门课程发表过评价");
        }
        
        // 过滤内容中的敏感词
        String filteredContent = contentFilterService.filterContent(reviewDto.getContent());
        
        Review review = new Review();
        review.setContent(filteredContent);
        review.setRating(reviewDto.getRating());
        review.setAnonymous(reviewDto.isAnonymous());
        review.setUser(user);
        review.setCourse(course);
        review.setCreatedAt(LocalDateTime.now());
        review.setPinned(false); // 新评论默认不置顶，忽略传入的置顶状态
        
        // 所有评论默认为已发布状态
        review.setStatus(Review.ReviewStatus.APPROVED);
        
        Review savedReview = reviewRepository.save(review);
        return convertToDto(savedReview);
    }
    
    @Override
    @Transactional
    public ReviewDto updateReview(ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + reviewDto.getId()));
        
        // 检查是否是评价的作者
        if (!review.getUser().getId().equals(reviewDto.getUserId())) {
            throw new IllegalArgumentException("您无权修改此评价");
        }
        
        // 过滤内容中的敏感词
        String filteredContent = contentFilterService.filterContent(reviewDto.getContent());
        
        review.setContent(filteredContent);
        review.setRating(reviewDto.getRating());
        review.setAnonymous(reviewDto.isAnonymous());
        review.setUpdatedAt(LocalDateTime.now());
        
        // 评论始终保持已发布状态
        review.setStatus(Review.ReviewStatus.APPROVED);
        
        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }
    
    @Override
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("评价不存在，ID: " + id);
        }
        reviewRepository.deleteById(id);
    }
    
    @Override
    public Map<String, Object> getCourseRatings(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
                
        List<Review> reviews = reviewRepository.findByCourse(course);
        
        return calculateRatings(reviews);
    }
    
    @Override
    public Map<String, Object> getTeacherRatings(Long teacherId) {
        List<Review> teacherReviews = reviewRepository.findByTeacherId(teacherId);
        
        return calculateRatings(teacherReviews);
    }
    
    @Override
    @Transactional
    public void approveReview(Long id) {
        // 不做任何操作，因为所有评论都默认已发布
    }
    
    @Override
    @Transactional
    public void rejectReview(Long id) {
        // 直接删除被拒绝的评论
        deleteReview(id);
    }
    
    @Override
    public List<ReviewDto> getReviewsByCourse(Long courseId, boolean approvedOnly) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        return reviewRepository.findByCourse(course).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getReviewsByTeacher(Long teacherId, boolean approvedOnly) {
        return reviewRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDto> getReviewsByStatus(Review.ReviewStatus status) {
        // 返回所有评论，忽略状态参数
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + id));
        
        // 过滤内容中的敏感词
        String filteredContent = contentFilterService.filterContent(reviewDto.getContent());
        
        review.setContent(filteredContent);
        review.setRating(reviewDto.getRating());
        review.setAnonymous(reviewDto.isAnonymous());
        review.setUpdatedAt(LocalDateTime.now());
        
        // 只有管理员可以更改置顶状态
        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + reviewDto.getUserId()));
                
        if (user.getRole() == User.Role.ROLE_ADMIN) {
            review.setPinned(reviewDto.isPinned());
        }
        
        // 评论始终保持已发布状态
        review.setStatus(Review.ReviewStatus.APPROVED);
        
        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }
    
    @Override
    @Transactional
    public ReviewDto updateReviewStatus(Long id, Review.ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + id));
        
        // 如果状态是REJECTED，则直接删除评论，否则保持APPROVED状态
        if (status == Review.ReviewStatus.REJECTED) {
            deleteReview(id);
            return null;
        } else {
            review.setStatus(Review.ReviewStatus.APPROVED);
            Review updatedReview = reviewRepository.save(review);
            return convertToDto(updatedReview);
        }
    }
    
    @Override
    @Transactional
    public ReviewDto pinReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + id));
        
        // 检查是否是管理员
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("只有管理员可以置顶评价");
        }
        
        review.setPinned(true);
        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }
    
    @Override
    @Transactional
    public ReviewDto unpinReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在，ID: " + id));
        
        // 检查是否是管理员
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("只有管理员可以取消置顶评价");
        }
        
        review.setPinned(false);
        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }
    
    // 获取当前登录用户
    private User getCurrentUser() {
        // 从Spring Security中获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));
    }
    
    @Override
    public ReviewDto getUserReviewForCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        return reviewRepository.findByUserAndCourse(user, course)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setAnonymous(review.isAnonymous());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setStatus(review.getStatus());
        dto.setPinned(review.isPinned());
        
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.isAnonymous() ? "匿名用户" : review.getUser().getUsername());
        
        if (!review.isAnonymous() && review.getUser().getRole() == User.Role.ROLE_ADMIN) {
            dto.setAdminReview(true);
        }
        
        dto.setCourseId(review.getCourse().getId());
        dto.setCourseName(review.getCourse().getName());
        dto.setCourseCode(review.getCourse().getCode());
        
        // 填充投票数据
        populateVoteData(dto, review.getId(), null);
        
        return dto;
    }
    
    /**
     * 填充评论的投票数据
     * @param dto ReviewDto对象
     * @param reviewId 评论ID
     * @param userId 当前用户ID（可选，用于查询用户的投票状态）
     */
    private void populateVoteData(ReviewDto dto, Long reviewId, Long userId) {
        // 统计点赞数
        Long likeCount = reviewVoteRepository.countByReviewIdAndVoteType(reviewId, ReviewVote.VoteType.LIKE);
        dto.setLikeCount(likeCount);
        
        // 统计踩数
        Long dislikeCount = reviewVoteRepository.countByReviewIdAndVoteType(reviewId, ReviewVote.VoteType.DISLIKE);
        dto.setDislikeCount(dislikeCount);
        
        // 如果提供了用户ID，查询用户的投票状态
        if (userId != null) {
            reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId)
                .ifPresent(vote -> dto.setUserVote(vote.getVoteType().name()));
        }
    }
    
    private Map<String, Object> calculateRatings(List<Review> reviews) {
        Map<String, Object> result = new HashMap<>();
        
        if (reviews.isEmpty()) {
            result.put("averageRating", 0.0);
            result.put("totalReviews", 0);
            result.put("ratingDistribution", new HashMap<Integer, Integer>());
            return result;
        }
        
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        
        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        
        // 确保所有评分都有数据
        Map<Integer, Integer> formattedDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            formattedDistribution.put(i, distribution.getOrDefault(i, 0L).intValue());
        }
        
        result.put("averageRating", Math.round(averageRating * 10) / 10.0);
        result.put("totalReviews", reviews.size());
        result.put("ratingDistribution", formattedDistribution);
        
        return result;
    }
    
    @Override
    @Transactional
    public ReviewDto voteReview(Long reviewId, Long userId, String voteType) {
        // 验证评论是否存在
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + reviewId));
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 验证投票类型
        ReviewVote.VoteType voteTypeEnum;
        try {
            voteTypeEnum = ReviewVote.VoteType.valueOf(voteType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的投票类型: " + voteType);
        }
        
        // 查找用户是否已经对该评论投票
        Optional<ReviewVote> existingVote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId);
        
        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();
            if (vote.getVoteType() == voteTypeEnum) {
                // 如果投票类型相同，则取消投票
                reviewVoteRepository.delete(vote);
            } else {
                // 如果投票类型不同，则更改投票
                vote.setVoteType(voteTypeEnum);
                vote.setCreatedAt(LocalDateTime.now());
                reviewVoteRepository.save(vote);
            }
        } else {
            // 创建新的投票
            ReviewVote newVote = new ReviewVote();
            newVote.setReview(review);
            newVote.setUser(user);
            newVote.setVoteType(voteTypeEnum);
            newVote.setCreatedAt(LocalDateTime.now());
            reviewVoteRepository.save(newVote);
        }
        
        // 返回更新后的评论DTO
        ReviewDto dto = convertToDto(review);
        populateVoteData(dto, reviewId, userId);
        return dto;
    }
    
    @Override
    @Transactional
    public ReviewDto cancelVote(Long reviewId, Long userId) {
        // 验证评论是否存在
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + reviewId));
        
        // 删除投票（如果存在）
        reviewVoteRepository.deleteByReviewIdAndUserId(reviewId, userId);
        
        // 返回更新后的评论DTO
        ReviewDto dto = convertToDto(review);
        populateVoteData(dto, reviewId, userId);
        return dto;
    }
} 