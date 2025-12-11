package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    
    private Long id;
    
    @NotBlank(message = "评价内容不能为空")
    private String content;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;
    
    private boolean anonymous;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Review.ReviewStatus status;
    
    private boolean pinned;
    
    // 标记是否为管理员评论
    private boolean isAdminReview;
    
    // 用户ID（创建时可不填，由Controller自动填充）
    private Long userId;
    
    private String username;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    private String courseName;
    
    private String courseCode;
    
    // 投票相关字段
    private Long likeCount = 0L;
    
    private Long dislikeCount = 0L;
    
    private String userVote; // "LIKE", "DISLIKE" 或 null
    
    public static ReviewDto fromEntity(Review review) {
        if (review == null) {
            return null;
        }
        
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setAnonymous(review.isAnonymous());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setStatus(review.getStatus());
        dto.setPinned(review.isPinned());
        
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUsername(review.isAnonymous() ? "匿名用户" : review.getUser().getUsername());
            // 设置是否为管理员评论，匿名时不暴露管理员身份
            dto.setAdminReview(!review.isAnonymous() && review.getUser().getRole() == User.Role.ROLE_ADMIN);
        }
        
        if (review.getCourse() != null) {
            dto.setCourseId(review.getCourse().getId());
            dto.setCourseName(review.getCourse().getName());
            dto.setCourseCode(review.getCourse().getCode());
        }
        
        return dto;
    }
} 