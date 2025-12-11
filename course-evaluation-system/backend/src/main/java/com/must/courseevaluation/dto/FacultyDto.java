package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.Faculty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDto {
    
    private Long id;
    
    @NotBlank(message = "学院名称不能为空")
    @Size(max = 100, message = "学院名称不能超过100个字符")
    private String name;
    
    private String description;
    
    public static FacultyDto fromEntity(Faculty faculty) {
        if (faculty == null) {
            return null;
        }
        
        FacultyDto dto = new FacultyDto();
        dto.setId(faculty.getId());
        dto.setName(faculty.getName());
        dto.setDescription(faculty.getDescription());
        return dto;
    }
} 