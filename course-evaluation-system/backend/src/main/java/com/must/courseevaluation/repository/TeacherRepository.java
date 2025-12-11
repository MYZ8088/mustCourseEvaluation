package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByName(String name);
    List<Teacher> findByNameContaining(String name);
    List<Teacher> findByFacultyId(Long facultyId);
    boolean existsByName(String name);
    
    /**
     * 获取所有教师（包含院系信息）
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.faculty")
    List<Teacher> findAllWithFaculty();
    
    /**
     * 获取每个教师的课程数量
     * 返回 Object[] 数组：[teacherId, courseCount]
     */
    @Query("SELECT t.id, COUNT(c.id) FROM Teacher t LEFT JOIN t.courses c GROUP BY t.id")
    List<Object[]> findTeacherCourseCounts();
    
    /**
     * 获取教师详情（包含课程列表）
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.faculty LEFT JOIN FETCH t.courses WHERE t.id = :id")
    Optional<Teacher> findByIdWithCourses(Long id);
} 