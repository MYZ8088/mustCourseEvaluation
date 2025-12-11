package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    List<Course> findByNameContaining(String name);
    List<Course> findByFaculty(Faculty faculty);
    List<Course> findByTeacher(Teacher teacher);
    List<Course> findByType(Course.CourseType type);
    boolean existsByCode(String code);
    
    /**
     * 获取所有课程及其关联数据（简化版，避免复杂聚合）
     */
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.faculty " +
           "LEFT JOIN FETCH c.teacher")
    List<Course> findAllWithRelations();
    
    /**
     * 获取单个课程及其关联数据（避免N+1查询）
     */
    @Query("SELECT c FROM Course c " +
           "LEFT JOIN FETCH c.faculty " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.id = :id")
    Optional<Course> findByIdWithRelations(Long id);
    
    /**
     * 获取指定院系的课程
     */
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.faculty " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.faculty.id = :facultyId")
    List<Course> findByFacultyIdWithRelations(Long facultyId);
    
    /**
     * 获取指定教师的课程
     */
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.faculty " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.teacher.id = :teacherId")
    List<Course> findByTeacherIdWithRelations(Long teacherId);
} 