package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.CourseScheduleDto;
import com.must.courseevaluation.model.Course;

import java.util.List;

public interface CourseService {
    List<CourseDto> getAllCourses();
    CourseDto getCourseById(Long id);
    List<CourseDto> getCoursesByFaculty(Long facultyId);
    List<CourseDto> getCoursesByTeacher(Long teacherId);
    List<CourseDto> getCoursesByType(Course.CourseType type);
    List<CourseDto> searchCourses(String keyword);
    CourseDto createCourse(CourseDto courseDto);
    CourseDto updateCourse(CourseDto courseDto);
    void deleteCourse(Long id);
    
    // 课程时间表相关方法
    /**
     * 获取课程的时间安排
     */
    List<CourseScheduleDto> getCourseSchedules(Long courseId);
    
    /**
     * 添加课程时间安排
     */
    CourseScheduleDto addCourseSchedule(Long courseId, CourseScheduleDto scheduleDto);
    
    /**
     * 更新课程时间安排
     */
    CourseScheduleDto updateCourseSchedule(Long scheduleId, CourseScheduleDto scheduleDto);
    
    /**
     * 删除课程时间安排
     */
    void deleteCourseSchedule(Long scheduleId);
    
    /**
     * 批量设置课程时间安排（先删除再添加）
     */
    List<CourseScheduleDto> setCourseSchedules(Long courseId, List<CourseScheduleDto> scheduleDtos);
    
    /**
     * 根据时间段查找课程（用于AI推荐时排除冲突）
     */
    List<CourseDto> findCoursesBySchedule(Integer dayOfWeek, Integer timePeriod);
    
    /**
     * 查找与用户时间表不冲突的课程
     */
    List<CourseDto> findCoursesWithoutConflict(Long userId);
} 