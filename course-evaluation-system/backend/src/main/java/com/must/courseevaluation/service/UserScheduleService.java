package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.UserScheduleDto;

import java.util.List;

public interface UserScheduleService {
    
    /**
     * 获取用户的所有课程时间安排
     */
    List<UserScheduleDto> getUserSchedules(Long userId);
    
    /**
     * 添加用户课程时间安排
     */
    UserScheduleDto addUserSchedule(Long userId, UserScheduleDto scheduleDto);
    
    /**
     * 更新用户课程时间安排
     */
    UserScheduleDto updateUserSchedule(Long scheduleId, UserScheduleDto scheduleDto);
    
    /**
     * 删除用户课程时间安排
     */
    void deleteUserSchedule(Long scheduleId);
    
    /**
     * 删除用户的所有课程时间安排
     */
    void deleteAllUserSchedules(Long userId);
    
    /**
     * 检查用户在指定时间是否有时间冲突
     */
    boolean hasTimeConflict(Long userId, Integer dayOfWeek, Integer timePeriod);
    
    /**
     * 检查课程时间与用户现有安排是否有冲突
     * @return 有冲突的时间段列表
     */
    List<UserScheduleDto> getConflictingSchedules(Long userId, List<Integer> dayOfWeeks, List<Integer> timePeriods);
    
    /**
     * 批量添加用户课程时间安排
     */
    List<UserScheduleDto> batchAddUserSchedules(Long userId, List<UserScheduleDto> scheduleDtos);
}

