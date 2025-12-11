import http from './http.service'

class ScheduleService {
  // ==================== 用户课程表相关 ====================
  
  // 获取当前用户的课程时间安排
  getMySchedules() {
    return http.get('user-schedules')
  }
  
  // 添加课程时间安排
  addSchedule(scheduleData) {
    return http.post('user-schedules', scheduleData)
  }
  
  // 批量添加课程时间安排
  batchAddSchedules(schedulesData) {
    return http.post('user-schedules/batch', schedulesData)
  }
  
  // 更新课程时间安排
  updateSchedule(scheduleId, scheduleData) {
    return http.put(`user-schedules/${scheduleId}`, scheduleData)
  }
  
  // 删除课程时间安排
  deleteSchedule(scheduleId) {
    return http.delete(`user-schedules/${scheduleId}`)
  }
  
  // 清空所有课程时间安排
  clearAllSchedules() {
    return http.delete('user-schedules/clear')
  }
  
  // 检查时间是否有冲突
  checkConflict(dayOfWeek, timePeriod) {
    return http.get('user-schedules/check-conflict', {
      params: { dayOfWeek, timePeriod }
    })
  }
  
  // 获取时间段选项
  getTimePeriods() {
    return http.get('user-schedules/time-periods')
  }
  
  // 获取星期选项
  getDaysOfWeek() {
    return http.get('user-schedules/days-of-week')
  }
  
  // ==================== 课程时间表相关 ====================
  
  // 获取课程的时间安排
  getCourseSchedules(courseId) {
    return http.get(`courses/${courseId}/schedules`)
  }
  
  // 添加课程时间安排（管理员用）
  addCourseSchedule(courseId, scheduleData) {
    return http.post(`courses/${courseId}/schedules`, scheduleData)
  }
  
  // 批量设置课程时间安排（管理员用）
  setCourseSchedules(courseId, schedulesData) {
    return http.put(`courses/${courseId}/schedules`, schedulesData)
  }
  
  // 删除课程时间安排（管理员用）
  deleteCourseSchedule(scheduleId) {
    return http.delete(`courses/schedules/${scheduleId}`)
  }
  
  // 根据时间段查找课程
  findCoursesBySchedule(dayOfWeek, timePeriod) {
    return http.get('courses/by-schedule', {
      params: { dayOfWeek, timePeriod }
    })
  }
  
  // 查找与当前用户时间表不冲突的课程
  findCoursesWithoutConflict() {
    return http.get('courses/without-conflict')
  }
  
  // ==================== 辅助方法 ====================
  
  // 时间段映射
  getTimePeriodInfo(period) {
    const periods = {
      1: { timeRange: '09:00-11:50', description: '上午' },
      2: { timeRange: '12:30-15:20', description: '下午早' },
      3: { timeRange: '15:30-18:20', description: '下午晚' },
      4: { timeRange: '19:00-21:50', description: '晚上' }
    }
    return periods[period] || { timeRange: '未知', description: '未知' }
  }
  
  // 星期映射
  getDayOfWeekName(day) {
    const days = {
      1: '周一',
      2: '周二',
      3: '周三',
      4: '周四',
      5: '周五',
      6: '周六',
      7: '周日'
    }
    return days[day] || '未知'
  }
  
  // 获取完整时间描述
  getFullTimeDescription(dayOfWeek, timePeriod) {
    const dayName = this.getDayOfWeekName(dayOfWeek)
    const periodInfo = this.getTimePeriodInfo(timePeriod)
    return `${dayName} ${periodInfo.timeRange}`
  }
}

export default new ScheduleService()

