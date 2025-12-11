package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    
    private Long id;
    
    @NotBlank(message = "教师姓名不能为空")
    @Size(max = 100, message = "教师姓名不能超过100个字符")
    private String name;
    
    private String title;
    
    @Email(message = "请提供有效的电子邮件地址")
    private String email;
    
    private String researchField;
    
    private String achievements;
    
    @NotNull(message = "院系ID不能为空")
    private Long facultyId;
    
    private String facultyName;
    
    // 新增：课程数量（用于列表展示，避免N+1查询）
    private Integer courseCount;
    
    public static TeacherDto fromEntity(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setTitle(teacher.getTitle());
        dto.setEmail(teacher.getEmail());
        dto.setResearchField(teacher.getResearchField());
        dto.setAchievements(teacher.getAchievements());
        if (teacher.getFaculty() != null) {
            dto.setFacultyId(teacher.getFaculty().getId());
            dto.setFacultyName(teacher.getFaculty().getName());
        }
        // 课程数量默认为0，由调用方单独设置（避免懒加载问题）
        dto.setCourseCount(0);
        return dto;
    }
    
    /**
     * 从实体转换，并设置课程数量
     */
    public static TeacherDto fromEntity(Teacher teacher, int courseCount) {
        TeacherDto dto = fromEntity(teacher);
        if (dto != null) {
            dto.setCourseCount(courseCount);
        }
        return dto;
    }
} 