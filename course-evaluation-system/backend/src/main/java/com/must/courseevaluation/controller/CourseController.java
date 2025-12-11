package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.CourseScheduleDto;
import com.must.courseevaluation.dto.CourseSummaryDto;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.security.UserDetailsImpl;
import com.must.courseevaluation.service.CourseService;
import com.must.courseevaluation.service.CourseSummaryService;
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

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CourseSummaryService courseSummaryService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<CourseDto>> getCoursesByFaculty(@PathVariable Long facultyId) {
        List<CourseDto> courses = courseService.getCoursesByFaculty(facultyId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDto>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<CourseDto> courses = courseService.getCoursesByTeacher(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CourseDto>> getCoursesByType(@PathVariable String type) {
        try {
            Course.CourseType courseType = Course.CourseType.valueOf(type);
            List<CourseDto> courses = courseService.getCoursesByType(courseType);
            return ResponseEntity.ok(courses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDto>> searchCourses(@RequestParam String keyword) {
        List<CourseDto> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        courseDto.setId(id);
        CourseDto updatedCourse = courseService.updateCourse(courseDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<Map<String, Object>> getCourseRatings(@PathVariable Long id) {
        Map<String, Object> ratings = reviewService.getCourseRatings(id);
        return ResponseEntity.ok(ratings);
    }

    /**
     * 获取课程AI总结（只返回缓存的总结，不触发生成）
     * 任何用户都可以访问
     * AI总结在后端启动时自动生成，此端点只负责读取缓存
     */
    @GetMapping("/{id}/ai-summary")
    public ResponseEntity<?> getCourseSummary(@PathVariable Long id) {
        try {
            int reviewCount = courseSummaryService.getReviewCount(id);
            
            // 评论不足10条
            if (reviewCount < 10) {
                return ResponseEntity.ok(Map.of(
                        "available", false,
                        "reviewCount", reviewCount,
                        "message", "评价不足10条，暂无AI总结"
                ));
            }
            
            // 获取缓存的总结（不会触发生成）
            CourseSummaryDto summary = courseSummaryService.getCourseSummary(id);
            if (summary != null) {
                return ResponseEntity.ok(Map.of(
                        "available", true,
                        "reviewCount", reviewCount,
                        "summary", summary
                ));
            }
            
            // 没有缓存的总结（可能是启动时尚未生成，或AI服务不可用）
            boolean aiAvailable = courseSummaryService.isAIServiceAvailable();
            String message = aiAvailable 
                    ? "AI总结正在生成中，请稍后刷新页面" 
                    : "AI服务未配置，暂无法生成总结";
            return ResponseEntity.ok(Map.of(
                    "available", false,
                    "reviewCount", reviewCount,
                    "message", message
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 强制重新生成课程AI总结（管理员专用）
     */
    @PostMapping("/{id}/ai-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> regenerateCourseSummary(@PathVariable Long id) {
        try {
            if (!courseSummaryService.isAIServiceAvailable()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "AI服务未配置或未启用"));
            }
            CourseSummaryDto summary = courseSummaryService.regenerateCourseSummary(id);
            int reviewCount = courseSummaryService.getReviewCount(id);
            summary.setReviewCount(reviewCount);
            return ResponseEntity.ok(Map.of(
                    "available", true,
                    "reviewCount", reviewCount,
                    "summary", summary
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 检查AI服务状态
     */
    @GetMapping("/ai-status")
    public ResponseEntity<Map<String, Object>> getAIServiceStatus() {
        boolean available = courseSummaryService.isAIServiceAvailable();
        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "AI服务正常" : "AI服务未配置"
        ));
    }
    
    // ==================== 课程时间表相关API ====================
    
    /**
     * 获取课程的时间安排
     */
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<CourseScheduleDto>> getCourseSchedules(@PathVariable Long id) {
        List<CourseScheduleDto> schedules = courseService.getCourseSchedules(id);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 添加课程时间安排（管理员用）
     */
    @PostMapping("/{id}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseScheduleDto> addCourseSchedule(
            @PathVariable Long id,
            @Valid @RequestBody CourseScheduleDto scheduleDto) {
        CourseScheduleDto created = courseService.addCourseSchedule(id, scheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 批量设置课程时间安排（管理员用）
     */
    @PutMapping("/{id}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseScheduleDto>> setCourseSchedules(
            @PathVariable Long id,
            @Valid @RequestBody List<CourseScheduleDto> scheduleDtos) {
        List<CourseScheduleDto> schedules = courseService.setCourseSchedules(id, scheduleDtos);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 删除课程时间安排（管理员用）
     */
    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourseSchedule(@PathVariable Long scheduleId) {
        courseService.deleteCourseSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 根据时间段查找课程
     */
    @GetMapping("/by-schedule")
    public ResponseEntity<List<CourseDto>> findCoursesBySchedule(
            @RequestParam Integer dayOfWeek,
            @RequestParam Integer timePeriod) {
        List<CourseDto> courses = courseService.findCoursesBySchedule(dayOfWeek, timePeriod);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * 查找与当前用户时间表不冲突的课程（用于AI推荐）
     */
    @GetMapping("/without-conflict")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CourseDto>> findCoursesWithoutConflict(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CourseDto> courses = courseService.findCoursesWithoutConflict(userDetails.getId());
        return ResponseEntity.ok(courses);
    }
} 