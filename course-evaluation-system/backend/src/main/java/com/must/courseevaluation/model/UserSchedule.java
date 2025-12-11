package com.must.courseevaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "day_of_week", "time_period"})
})
public class UserSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=周一, 2=周二, ..., 7=周日
    
    @Column(name = "time_period", nullable = false)
    private Integer timePeriod; // 1-4 对应四个时间段
    
    @Column(name = "course_name", length = 100)
    private String courseName; // 课程名称（用户自填）
    
    /**
     * 获取时间段描述
     */
    public String getTimePeriodDescription() {
        return CourseSchedule.TimePeriod.fromValue(this.timePeriod).getTimeRange();
    }
    
    /**
     * 获取星期描述
     */
    public String getDayOfWeekDescription() {
        return CourseSchedule.DayOfWeek.fromValue(this.dayOfWeek).getChineseName();
    }
}

