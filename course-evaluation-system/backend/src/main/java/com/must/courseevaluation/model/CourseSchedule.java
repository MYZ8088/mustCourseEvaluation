package com.must.courseevaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "day_of_week", "time_period"})
})
public class CourseSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=周一, 2=周二, ..., 7=周日
    
    @Column(name = "time_period", nullable = false)
    private Integer timePeriod; // 1-4 对应四个时间段
    
    @Column(length = 100)
    private String location; // 上课地点
    
    /**
     * 时间段枚举
     * 1: 09:00-11:50 (上午)
     * 2: 12:30-15:20 (下午早)
     * 3: 15:30-18:20 (下午晚)
     * 4: 19:00-21:50 (晚上)
     */
    public enum TimePeriod {
        PERIOD_1(1, "09:00", "11:50", "上午"),
        PERIOD_2(2, "12:30", "15:20", "下午早"),
        PERIOD_3(3, "15:30", "18:20", "下午晚"),
        PERIOD_4(4, "19:00", "21:50", "晚上");
        
        private final int value;
        private final String startTime;
        private final String endTime;
        private final String description;
        
        TimePeriod(int value, String startTime, String endTime, String description) {
            this.value = value;
            this.startTime = startTime;
            this.endTime = endTime;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getStartTime() {
            return startTime;
        }
        
        public String getEndTime() {
            return endTime;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getTimeRange() {
            return startTime + "-" + endTime;
        }
        
        public static TimePeriod fromValue(int value) {
            for (TimePeriod period : values()) {
                if (period.value == value) {
                    return period;
                }
            }
            throw new IllegalArgumentException("无效的时间段值: " + value + "，有效值为1-4");
        }
    }
    
    /**
     * 星期枚举
     */
    public enum DayOfWeek {
        MONDAY(1, "周一"),
        TUESDAY(2, "周二"),
        WEDNESDAY(3, "周三"),
        THURSDAY(4, "周四"),
        FRIDAY(5, "周五"),
        SATURDAY(6, "周六"),
        SUNDAY(7, "周日");
        
        private final int value;
        private final String chineseName;
        
        DayOfWeek(int value, String chineseName) {
            this.value = value;
            this.chineseName = chineseName;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getChineseName() {
            return chineseName;
        }
        
        public static DayOfWeek fromValue(int value) {
            for (DayOfWeek day : values()) {
                if (day.value == value) {
                    return day;
                }
            }
            throw new IllegalArgumentException("无效的星期值: " + value + "，有效值为1-7(周一至周日)");
        }
    }
    
    /**
     * 获取时间段描述
     */
    public String getTimePeriodDescription() {
        TimePeriod period = TimePeriod.fromValue(this.timePeriod);
        return period.getTimeRange() + " (" + period.getDescription() + ")";
    }
    
    /**
     * 获取星期描述
     */
    public String getDayOfWeekDescription() {
        return DayOfWeek.fromValue(this.dayOfWeek).getChineseName();
    }
}

