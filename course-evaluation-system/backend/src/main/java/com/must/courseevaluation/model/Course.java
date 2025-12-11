package com.must.courseevaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double credits = 3.0;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseType type = CourseType.COMPULSORY;
    
    @Column(length = 2000)
    private String assessmentCriteria;
    
    // AI总结内容（JSON格式存储）
    @Column(columnDefinition = "TEXT")
    private String aiSummary;
    
    // AI总结更新时间
    @Column
    private LocalDateTime aiSummaryUpdatedAt;
    
    // 生成AI总结时的评论数量（用于判断是否需要重新生成）
    @Column
    private Integer aiSummaryReviewCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();
    
    public enum CourseType {
        COMPULSORY("必修课"),
        ELECTIVE("选修课");
        
        private final String chineseName;
        
        CourseType(String chineseName) {
            this.chineseName = chineseName;
        }
        
        public String getChineseName() {
            return chineseName;
        }
    }
} 