package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    
    /**
     * 根据课程ID查找所有上课时间
     */
    List<CourseSchedule> findByCourseId(Long courseId);
    
    /**
     * 根据星期几查找所有课程时间
     */
    List<CourseSchedule> findByDayOfWeek(Integer dayOfWeek);
    
    /**
     * 根据时间段查找所有课程时间
     */
    List<CourseSchedule> findByTimePeriod(Integer timePeriod);
    
    /**
     * 根据星期几和时间段查找课程时间
     */
    List<CourseSchedule> findByDayOfWeekAndTimePeriod(Integer dayOfWeek, Integer timePeriod);
    
    /**
     * 删除课程的所有时间安排
     */
    void deleteByCourseId(Long courseId);
    
    /**
     * 检查某课程在指定时间是否已有安排
     */
    boolean existsByCourseIdAndDayOfWeekAndTimePeriod(Long courseId, Integer dayOfWeek, Integer timePeriod);
}

