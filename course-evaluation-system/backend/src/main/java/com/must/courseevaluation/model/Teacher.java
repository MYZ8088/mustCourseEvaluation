package com.must.courseevaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teachers")
public class Teacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String title;
    
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String researchField;
    
    @Column(columnDefinition = "TEXT")
    private String achievements;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
    
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<Course> courses = new HashSet<>();
} 