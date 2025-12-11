package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.CourseSchedule;
import com.must.courseevaluation.model.UserSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScheduleDto {
    
    private Long id;
    
    private Long userId;
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几最小为1（周一）")
    @Max(value = 7, message = "星期几最大为7（周日）")
    private Integer dayOfWeek;
    
    @NotNull(message = "时间段不能为空")
    @Min(value = 1, message = "时间段最小为1")
    @Max(value = 4, message = "时间段最大为4")
    private Integer timePeriod;
    
    @Size(max = 100, message = "课程名称不能超过100个字符")
    private String courseName;
    
    // 显示用的描述字段
    private String dayOfWeekName;
    private String timeRange;
    private String timePeriodName;
    
    /**
     * 从实体转换为DTO
     */
    public static UserScheduleDto fromEntity(UserSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        
        UserScheduleDto dto = new UserScheduleDto();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setTimePeriod(schedule.getTimePeriod());
        dto.setCourseName(schedule.getCourseName());
        
        if (schedule.getUser() != null) {
            dto.setUserId(schedule.getUser().getId());
        }
        
        // 设置描述字段
        CourseSchedule.DayOfWeek day = CourseSchedule.DayOfWeek.fromValue(schedule.getDayOfWeek());
        dto.setDayOfWeekName(day.getChineseName());
        
        CourseSchedule.TimePeriod period = CourseSchedule.TimePeriod.fromValue(schedule.getTimePeriod());
        dto.setTimeRange(period.getTimeRange());
        dto.setTimePeriodName(period.getDescription());
        
        return dto;
    }
    
    /**
     * 获取完整的时间描述
     */
    public String getFullDescription() {
        return dayOfWeekName + " " + timeRange + (courseName != null ? " - " + courseName : "");
    }
}

