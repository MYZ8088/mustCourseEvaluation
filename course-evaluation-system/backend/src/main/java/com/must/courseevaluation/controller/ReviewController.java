package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.security.UserDetailsImpl;
import com.must.courseevaluation.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByCourse(@PathVariable Long courseId) {
        List<ReviewDto> reviews = reviewService.getReviewsByCourse(courseId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByTeacher(@PathVariable Long teacherId) {
        List<ReviewDto> reviews = reviewService.getReviewsByTeacher(teacherId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("@userSecurity.isUserSelf(#userId, principal) or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto reviewDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 检查用户是否被禁言
        if (!userDetails.isCanComment()) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(null); // 或者返回包含错误信息的DTO
        }
        
        reviewDto.setUserId(userDetails.getId());
        ReviewDto createdReview = reviewService.createReview(reviewDto);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.isUserSelf(#reviewOwnerId, principal) or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDto reviewDto, @RequestParam Long reviewOwnerId) {
        reviewDto.setUserId(reviewOwnerId);
        ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurity.isUserSelf(#reviewOwnerId, principal) or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @RequestParam Long reviewOwnerId) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> adminDeleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> batchDeleteReviews(@RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(reviewService::deleteReview);
        }
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/course/{courseId}/ratings")
    public ResponseEntity<Map<String, Object>> getCourseRatings(@PathVariable Long courseId) {
        Map<String, Object> ratings = reviewService.getCourseRatings(courseId);
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/teacher/{teacherId}/ratings")
    public ResponseEntity<Map<String, Object>> getTeacherRatings(@PathVariable Long teacherId) {
        Map<String, Object> ratings = reviewService.getTeacherRatings(teacherId);
        return ResponseEntity.ok(ratings);
    }
    
    /**
     * 检查当前用户是否已评论某课程，返回评论信息（如果存在）
     */
    @GetMapping("/check/{courseId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> checkUserReview(@PathVariable Long courseId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewDto existingReview = reviewService.getUserReviewForCourse(userDetails.getId(), courseId);
        
        if (existingReview != null) {
            return ResponseEntity.ok(Map.of(
                "hasReview", true,
                "review", existingReview
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "hasReview", false
            ));
        }
    }
    
    @PatchMapping("/{id}/pin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReviewDto> pinReview(@PathVariable Long id) {
        ReviewDto pinnedReview = reviewService.pinReview(id);
        return ResponseEntity.ok(pinnedReview);
    }
    
    @PatchMapping("/{id}/unpin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ReviewDto> unpinReview(@PathVariable Long id) {
        ReviewDto unpinnedReview = reviewService.unpinReview(id);
        return ResponseEntity.ok(unpinnedReview);
    }
    
    /**
     * 对评论进行投票（点赞或踩）
     */
    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR')")
    public ResponseEntity<ReviewDto> voteReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> voteRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        String voteType = voteRequest.get("voteType");
        if (voteType == null || (!voteType.equals("LIKE") && !voteType.equals("DISLIKE"))) {
            return ResponseEntity.badRequest().build();
        }
        
        ReviewDto updatedReview = reviewService.voteReview(id, userDetails.getId(), voteType);
        return ResponseEntity.ok(updatedReview);
    }
    
    /**
     * 取消对评论的投票
     */
    @DeleteMapping("/{id}/vote")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR')")
    public ResponseEntity<ReviewDto> cancelVote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ReviewDto updatedReview = reviewService.cancelVote(id, userDetails.getId());
        return ResponseEntity.ok(updatedReview);
    }
} 