package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.UserScheduleDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.model.UserSchedule;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.repository.UserScheduleRepository;
import com.must.courseevaluation.service.UserScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserScheduleServiceImpl implements UserScheduleService {
    
    @Autowired
    private UserScheduleRepository userScheduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<UserScheduleDto> getUserSchedules(Long userId) {
        return userScheduleRepository.findByUserId(userId).stream()
                .map(UserScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserScheduleDto addUserSchedule(Long userId, UserScheduleDto scheduleDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 检查是否已存在相同时间的安排
        if (userScheduleRepository.existsByUserIdAndDayOfWeekAndTimePeriod(
                userId, scheduleDto.getDayOfWeek(), scheduleDto.getTimePeriod())) {
            throw new IllegalArgumentException("该时间段已有课程安排");
        }
        
        UserSchedule schedule = new UserSchedule();
        schedule.setUser(user);
        schedule.setDayOfWeek(scheduleDto.getDayOfWeek());
        schedule.setTimePeriod(scheduleDto.getTimePeriod());
        schedule.setCourseName(scheduleDto.getCourseName());
        
        UserSchedule savedSchedule = userScheduleRepository.save(schedule);
        return UserScheduleDto.fromEntity(savedSchedule);
    }
    
    @Override
    @Transactional
    public UserScheduleDto updateUserSchedule(Long scheduleId, UserScheduleDto scheduleDto) {
        UserSchedule schedule = userScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，ID: " + scheduleId));
        
        // 如果时间改变，检查是否冲突
        if (!schedule.getDayOfWeek().equals(scheduleDto.getDayOfWeek()) ||
            !schedule.getTimePeriod().equals(scheduleDto.getTimePeriod())) {
            if (userScheduleRepository.existsByUserIdAndDayOfWeekAndTimePeriod(
                    schedule.getUser().getId(), scheduleDto.getDayOfWeek(), scheduleDto.getTimePeriod())) {
                throw new IllegalArgumentException("该时间段已有课程安排");
            }
        }
        
        schedule.setDayOfWeek(scheduleDto.getDayOfWeek());
        schedule.setTimePeriod(scheduleDto.getTimePeriod());
        schedule.setCourseName(scheduleDto.getCourseName());
        
        UserSchedule updatedSchedule = userScheduleRepository.save(schedule);
        return UserScheduleDto.fromEntity(updatedSchedule);
    }
    
    @Override
    @Transactional
    public void deleteUserSchedule(Long scheduleId) {
        if (!userScheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("课程安排不存在，ID: " + scheduleId);
        }
        userScheduleRepository.deleteById(scheduleId);
    }
    
    @Override
    @Transactional
    public void deleteAllUserSchedules(Long userId) {
        userScheduleRepository.deleteByUserId(userId);
    }
    
    @Override
    public boolean hasTimeConflict(Long userId, Integer dayOfWeek, Integer timePeriod) {
        return userScheduleRepository.existsByUserIdAndDayOfWeekAndTimePeriod(userId, dayOfWeek, timePeriod);
    }
    
    @Override
    public List<UserScheduleDto> getConflictingSchedules(Long userId, List<Integer> dayOfWeeks, List<Integer> timePeriods) {
        List<UserScheduleDto> conflicts = new ArrayList<>();
        List<UserSchedule> userSchedules = userScheduleRepository.findByUserId(userId);
        
        for (int i = 0; i < dayOfWeeks.size(); i++) {
            int day = dayOfWeeks.get(i);
            int period = timePeriods.get(i);
            
            for (UserSchedule schedule : userSchedules) {
                if (schedule.getDayOfWeek() == day && schedule.getTimePeriod() == period) {
                    conflicts.add(UserScheduleDto.fromEntity(schedule));
                }
            }
        }
        
        return conflicts;
    }
    
    @Override
    @Transactional
    public List<UserScheduleDto> batchAddUserSchedules(Long userId, List<UserScheduleDto> scheduleDtos) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        List<UserScheduleDto> addedSchedules = new ArrayList<>();
        
        for (UserScheduleDto dto : scheduleDtos) {
            // 检查是否已存在
            if (!userScheduleRepository.existsByUserIdAndDayOfWeekAndTimePeriod(
                    userId, dto.getDayOfWeek(), dto.getTimePeriod())) {
                UserSchedule schedule = new UserSchedule();
                schedule.setUser(user);
                schedule.setDayOfWeek(dto.getDayOfWeek());
                schedule.setTimePeriod(dto.getTimePeriod());
                schedule.setCourseName(dto.getCourseName());
                
                UserSchedule savedSchedule = userScheduleRepository.save(schedule);
                addedSchedules.add(UserScheduleDto.fromEntity(savedSchedule));
            }
        }
        
        return addedSchedules;
    }
}

