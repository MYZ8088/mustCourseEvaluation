package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourse(Course course);
    Page<Review> findByCourse(Course course, Pageable pageable);
    List<Review> findByUser(User user);
    Page<Review> findByUser(User user, Pageable pageable);
    List<Review> findByCourseAndStatus(Course course, Review.ReviewStatus status);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course = ?1")
    Double getAverageRatingForCourse(Course course);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course = ?1")
    Long getReviewCountForCourse(Course course);
    
    @Query("SELECT r FROM Review r JOIN r.course c WHERE c.teacher.id = :teacherId AND r.status = :status")
    List<Review> findByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") Review.ReviewStatus status);
    
    @Query("SELECT r FROM Review r JOIN r.course c WHERE c.teacher.id = :teacherId")
    List<Review> findByTeacherId(@Param("teacherId") Long teacherId);
    
    boolean existsByUserAndCourse(User user, Course course);
    
    // 获取用户对某课程的评论
    java.util.Optional<Review> findByUserAndCourse(User user, Course course);
} 