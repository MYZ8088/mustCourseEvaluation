package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.UserScheduleDto;
import com.must.courseevaluation.security.UserDetailsImpl;
import com.must.courseevaluation.service.UserScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user-schedules")
public class UserScheduleController {
    
    @Autowired
    private UserScheduleService userScheduleService;
    
    /**
     * 获取当前用户的所有课程时间安排
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserScheduleDto>> getMySchedules(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserScheduleDto> schedules = userScheduleService.getUserSchedules(userDetails.getId());
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 获取指定用户的课程时间安排（管理员用）
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserScheduleDto>> getUserSchedules(@PathVariable Long userId) {
        List<UserScheduleDto> schedules = userScheduleService.getUserSchedules(userId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 添加课程时间安排
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserScheduleDto> addSchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserScheduleDto scheduleDto) {
        UserScheduleDto created = userScheduleService.addUserSchedule(userDetails.getId(), scheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 批量添加课程时间安排
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserScheduleDto>> batchAddSchedules(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody List<UserScheduleDto> scheduleDtos) {
        List<UserScheduleDto> created = userScheduleService.batchAddUserSchedules(userDetails.getId(), scheduleDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 更新课程时间安排
     */
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserScheduleDto> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UserScheduleDto scheduleDto) {
        UserScheduleDto updated = userScheduleService.updateUserSchedule(scheduleId, scheduleDto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 删除课程时间安排
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        userScheduleService.deleteUserSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 清空所有课程时间安排
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> clearAllSchedules(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userScheduleService.deleteAllUserSchedules(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查时间是否有冲突
     */
    @GetMapping("/check-conflict")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkConflict(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Integer dayOfWeek,
            @RequestParam Integer timePeriod) {
        // 参数验证
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            return ResponseEntity.badRequest().body(Map.of("error", "dayOfWeek必须在1-7之间"));
        }
        if (timePeriod == null || timePeriod < 1 || timePeriod > 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "timePeriod必须在1-4之间"));
        }
        
        boolean hasConflict = userScheduleService.hasTimeConflict(userDetails.getId(), dayOfWeek, timePeriod);
        return ResponseEntity.ok(Map.of("hasConflict", hasConflict));
    }
    
    /**
     * 获取时间段选项
     */
    @GetMapping("/time-periods")
    public ResponseEntity<List<Map<String, Object>>> getTimePeriods() {
        List<Map<String, Object>> periods = List.of(
            Map.of("value", 1, "timeRange", "09:00-11:50", "description", "上午"),
            Map.of("value", 2, "timeRange", "12:30-15:20", "description", "下午早"),
            Map.of("value", 3, "timeRange", "15:30-18:20", "description", "下午晚"),
            Map.of("value", 4, "timeRange", "19:00-21:50", "description", "晚上")
        );
        return ResponseEntity.ok(periods);
    }
    
    /**
     * 获取星期选项
     */
    @GetMapping("/days-of-week")
    public ResponseEntity<List<Map<String, Object>>> getDaysOfWeek() {
        List<Map<String, Object>> days = List.of(
            Map.of("value", 1, "name", "周一"),
            Map.of("value", 2, "name", "周二"),
            Map.of("value", 3, "name", "周三"),
            Map.of("value", 4, "name", "周四"),
            Map.of("value", 5, "name", "周五"),
            Map.of("value", 6, "name", "周六"),
            Map.of("value", 7, "name", "周日")
        );
        return ResponseEntity.ok(days);
    }
}

