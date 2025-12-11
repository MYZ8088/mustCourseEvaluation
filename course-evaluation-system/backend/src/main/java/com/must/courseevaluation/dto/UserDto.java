package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;
    
    @Email(message = "请提供有效的电子邮件地址")
    @NotBlank(message = "电子邮件不能为空")
    private String email;
    
    private String password;
    
    private String studentId;
    
    private String fullName;
    
    private User.Role role;
    
    private boolean active = true;
    
    private boolean canComment = true;
    
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStudentId(user.getStudentId());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCanComment(user.isCanComment());
        return dto;
    }
} 