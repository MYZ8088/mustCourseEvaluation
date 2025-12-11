package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.Course;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    
    private Long id;
    
    @NotBlank(message = "课程代码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "课程代码只能包含字母、数字和连字符")
    private String code;
    
    @NotBlank(message = "课程名称不能为空")
    private String name;
    
    @NotNull(message = "学分不能为空")
    @PositiveOrZero(message = "学分必须是非负数")
    private Double credits;
    
    private String description;
    
    @NotNull(message = "课程类型不能为空")
    private String type;
    
    @Size(max = 2000, message = "考核标准不能超过2000个字符")
    private String assessmentCriteria;
    
    @NotNull(message = "院系不能为空")
    private Long facultyId;
    
    private String facultyName;
    
    private Long teacherId;
    
    private String teacherName;
    
    private Double averageRating;
    
    private Long reviewCount;
    
    // AI总结内容（JSON格式）
    private String aiSummary;
    
    // AI总结更新时间
    private String aiSummaryUpdatedAt;
    
    // 生成AI总结时的评论数量
    private Integer aiSummaryReviewCount;
    
    // 课程时间安排
    private List<CourseScheduleDto> schedules = new ArrayList<>();
    
    public static CourseDto fromEntity(Course course) {
        if (course == null) {
            return null;
        }
        
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCode(course.getCode());
        dto.setName(course.getName());
        dto.setCredits(course.getCredits());
        dto.setDescription(course.getDescription());
        dto.setType(course.getType().name());
        dto.setAssessmentCriteria(course.getAssessmentCriteria());
        
        // 安全访问懒加载字段（使用 Hibernate.isInitialized 检查）
        try {
            if (course.getFaculty() != null) {
                dto.setFacultyId(course.getFaculty().getId());
                dto.setFacultyName(course.getFaculty().getName());
            }
        } catch (Exception e) {
            // 懒加载异常，跳过
        }
        
        try {
            if (course.getTeacher() != null) {
                dto.setTeacherId(course.getTeacher().getId());
                dto.setTeacherName(course.getTeacher().getName());
            }
        } catch (Exception e) {
            // 懒加载异常，跳过
        }
        
        // 设置AI总结相关字段
        dto.setAiSummary(course.getAiSummary());
        if (course.getAiSummaryUpdatedAt() != null) {
            dto.setAiSummaryUpdatedAt(course.getAiSummaryUpdatedAt().toString());
        }
        dto.setAiSummaryReviewCount(course.getAiSummaryReviewCount());
        
        // 评论统计默认为0，由调用方单独设置（避免懒加载问题）
        dto.setReviewCount(0L);
        dto.setAverageRating(null);
        
        return dto;
    }
} 