package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.CourseSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseScheduleDto {
    
    private Long id;
    
    private Long courseId;
    
    private String courseName;
    
    private String courseCode;
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几最小为1（周一）")
    @Max(value = 7, message = "星期几最大为7（周日）")
    private Integer dayOfWeek;
    
    @NotNull(message = "时间段不能为空")
    @Min(value = 1, message = "时间段最小为1")
    @Max(value = 4, message = "时间段最大为4")
    private Integer timePeriod;
    
    private String location;
    
    // 显示用的描述字段
    private String dayOfWeekName; // 如：周一
    private String timeRange;     // 如：09:00-11:50
    private String timePeriodName; // 如：上午
    
    /**
     * 从实体转换为DTO
     */
    public static CourseScheduleDto fromEntity(CourseSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        
        CourseScheduleDto dto = new CourseScheduleDto();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setTimePeriod(schedule.getTimePeriod());
        dto.setLocation(schedule.getLocation());
        
        if (schedule.getCourse() != null) {
            dto.setCourseId(schedule.getCourse().getId());
            dto.setCourseName(schedule.getCourse().getName());
            dto.setCourseCode(schedule.getCourse().getCode());
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
        return dayOfWeekName + " " + timeRange + (location != null ? " (" + location + ")" : "");
    }
    
    /**
     * 获取时间段的静态信息
     */
    public static class TimePeriodInfo {
        public static final int PERIOD_1 = 1;
        public static final int PERIOD_2 = 2;
        public static final int PERIOD_3 = 3;
        public static final int PERIOD_4 = 4;
        
        public static String getTimeRange(int period) {
            return CourseSchedule.TimePeriod.fromValue(period).getTimeRange();
        }
        
        public static String getDescription(int period) {
            return CourseSchedule.TimePeriod.fromValue(period).getDescription();
        }
    }
}

