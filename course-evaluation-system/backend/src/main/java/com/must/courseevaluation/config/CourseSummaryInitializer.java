package com.must.courseevaluation.config;

import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Review;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import com.must.courseevaluation.service.CourseSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时自动检查并生成AI课程总结
 */
@Component
public class CourseSummaryInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CourseSummaryInitializer.class);
    
    // 最小评论数阈值
    private static final int MIN_REVIEW_COUNT = 10;
    // 评论变化阈值（超过此值才重新生成）
    private static final int REVIEW_CHANGE_THRESHOLD = 10;

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final CourseSummaryService courseSummaryService;

    public CourseSummaryInitializer(CourseRepository courseRepository,
                                   ReviewRepository reviewRepository,
                                   CourseSummaryService courseSummaryService) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
        this.courseSummaryService = courseSummaryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("====== 开始检查AI课程总结 ======");
        
        // 检查AI服务是否可用
        if (!courseSummaryService.isAIServiceAvailable()) {
            logger.warn("AI服务未配置或未启用，跳过自动生成AI总结");
            return;
        }

        List<Course> allCourses = courseRepository.findAll();
        int generatedCount = 0;
        int skippedCount = 0;

        for (Course course : allCourses) {
            try {
                // 获取当前评论数量
                List<Review> approvedReviews = reviewRepository.findByCourseAndStatus(
                        course, Review.ReviewStatus.APPROVED);
                int currentReviewCount = approvedReviews.size();

                // 检查是否需要生成AI总结
                if (shouldGenerateSummary(course, currentReviewCount)) {
                    logger.info("为课程 {} ({}) 生成AI总结，当前评论数: {}", 
                            course.getName(), course.getCode(), currentReviewCount);
                    
                    // 调用服务生成总结
                    courseSummaryService.regenerateCourseSummary(course.getId());
                    generatedCount++;
                    
                    // 避免API调用过于频繁
                    Thread.sleep(1000);
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                logger.error("为课程 {} 生成AI总结失败: {}", course.getCode(), e.getMessage());
            }
        }

        logger.info("====== AI课程总结检查完成 ======");
        logger.info("总课程数: {}, 新生成: {}, 跳过: {}", 
                allCourses.size(), generatedCount, skippedCount);
    }

    /**
     * 判断是否需要生成AI总结
     * 
     * @param course 课程
     * @param currentReviewCount 当前评论数量
     * @return true 需要生成，false 不需要
     */
    private boolean shouldGenerateSummary(Course course, int currentReviewCount) {
        // 评论数不足10条，不生成
        if (currentReviewCount < MIN_REVIEW_COUNT) {
            return false;
        }

        // 没有AI总结，需要生成
        if (course.getAiSummary() == null || course.getAiSummary().isEmpty()) {
            return true;
        }

        // 检查评论数变化是否超过阈值
        Integer lastReviewCount = course.getAiSummaryReviewCount();
        if (lastReviewCount == null) {
            // 没有记录上次评论数，需要生成
            return true;
        }

        // 评论数变化超过阈值才重新生成
        int reviewChange = currentReviewCount - lastReviewCount;
        return reviewChange > REVIEW_CHANGE_THRESHOLD;
    }
}












