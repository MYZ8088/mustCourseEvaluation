package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.UserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    
    /**
     * 根据用户ID查找所有课程时间
     */
    List<UserSchedule> findByUserId(Long userId);
    
    /**
     * 根据用户ID和星期几查找课程时间
     */
    List<UserSchedule> findByUserIdAndDayOfWeek(Long userId, Integer dayOfWeek);
    
    /**
     * 检查用户在指定时间是否已有安排
     */
    boolean existsByUserIdAndDayOfWeekAndTimePeriod(Long userId, Integer dayOfWeek, Integer timePeriod);
    
    /**
     * 查找用户在指定时间的安排
     */
    Optional<UserSchedule> findByUserIdAndDayOfWeekAndTimePeriod(Long userId, Integer dayOfWeek, Integer timePeriod);
    
    /**
     * 删除用户的所有时间安排
     */
    void deleteByUserId(Long userId);
    
    /**
     * 统计用户的课程数量
     */
    long countByUserId(Long userId);
}

