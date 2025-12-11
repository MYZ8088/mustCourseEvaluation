package com.must.courseevaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @Column(name = "preferred_course_type", length = 20)
    private String preferredCourseType;  // 'COMPULSORY' 或 'ELECTIVE'
    
    @Column(name = "preferred_credits")
    private Integer preferredCredits;
    
    @Column(name = "preferred_difficulty", length = 20)
    private String preferredDifficulty;  // 'easy', 'medium', 'hard'
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interest_keywords", columnDefinition = "text[]")
    private List<String> interestKeywords = new ArrayList<>();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}





















