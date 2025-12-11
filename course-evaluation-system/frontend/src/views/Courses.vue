<template>
  <div class="courses-container">
    <div class="page-header">
      <h1>课程列表</h1>
      <router-link to="/" class="btn-back">
        <i class="fas fa-home"></i> 返回首页
      </router-link>
    </div>
    
    <!-- 搜索和过滤 -->
    <div class="filters">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索课程名称..." 
          @input="filterCourses"
        />
      </div>
      
      <div class="faculty-filter">
        <select v-model="selectedFaculty" @change="filterCourses">
          <option :value="null">所有院系</option>
          <option v-for="faculty in faculties" :key="faculty.id" :value="faculty.id">
            {{ faculty.name }}
          </option>
        </select>
      </div>
      
      <div class="type-filter">
        <select v-model="selectedType" @change="filterCourses">
          <option :value="null">所有类型</option>
          <option value="COMPULSORY">必修课</option>
          <option value="ELECTIVE">选修课</option>
        </select>
      </div>
    </div>
    
    <!-- 时间筛选 -->
    <div class="schedule-filter">
      <div class="filter-header">
        <span class="filter-title"><i class="fas fa-clock"></i> 上课时间筛选</span>
        <button v-if="hasScheduleFilter" @click="clearScheduleFilter" class="btn-clear">
          <i class="fas fa-times"></i> 清除时间筛选
        </button>
      </div>
      
      <div class="schedule-options">
        <!-- 星期选择 -->
        <div class="day-options">
          <span class="option-label">星期:</span>
          <div class="checkbox-group">
            <label v-for="day in dayOptions" :key="day.value" class="checkbox-item">
              <input 
                type="checkbox" 
                :value="day.value" 
                v-model="selectedDays"
                @change="filterCourses"
              />
              <span class="checkbox-text">{{ day.name }}</span>
            </label>
          </div>
        </div>
        
        <!-- 时间段选择 -->
        <div class="period-options">
          <span class="option-label">时间段:</span>
          <div class="checkbox-group">
            <label v-for="period in periodOptions" :key="period.value" class="checkbox-item">
              <input 
                type="checkbox" 
                :value="period.value" 
                v-model="selectedPeriods"
                @change="filterCourses"
              />
              <span class="checkbox-text">{{ period.timeRange }} ({{ period.description }})</span>
            </label>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 课程列表 -->
    <div class="courses-list" v-if="!loading">
      <div v-if="filteredCourses.length === 0" class="no-results">
        <p>没有找到符合条件的课程</p>
      </div>
      
      <div v-else class="courses-grid">
        <div 
          v-for="course in filteredCourses" 
          :key="course.id" 
          class="course-card"
          @click="viewCourseDetails(course.id)"
        >
          <div class="course-header">
            <span class="course-code">{{ course.code }}</span>
            <span :class="['course-type', course.type === 'COMPULSORY' ? 'compulsory' : 'elective']">
              {{ course.type === 'COMPULSORY' ? '必修' : '选修' }}
            </span>
          </div>
          <div class="course-info">
            <h3>{{ course.name }}</h3>
            <p class="course-faculty">{{ course.facultyName }}</p>
            <p class="course-teacher" v-if="course.teacherName">
              授课教师: {{ course.teacherName }}
            </p>
            <p class="course-credits">
              学分: {{ course.credits }}
            </p>
            <p class="course-description" v-if="course.description">
              {{ truncate(course.description, 100) }}
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="loading">
      <p>加载中，请稍候...</p>
    </div>
  </div>
</template>

<script>
import courseService from '@/services/course.service';
import facultyService from '@/services/faculty.service';
import scheduleService from '@/services/schedule.service';

export default {
  name: 'Courses',
  data() {
    return {
      loading: true,
      courses: [],
      faculties: [],
      courseSchedules: {}, // 课程ID -> 时间安排列表的映射
      searchQuery: '',
      selectedFaculty: null,
      selectedType: null,
      filteredCourses: [],
      
      // 时间筛选选项
      selectedDays: [],
      selectedPeriods: [],
      dayOptions: [
        { value: 1, name: '周一' },
        { value: 2, name: '周二' },
        { value: 3, name: '周三' },
        { value: 4, name: '周四' },
        { value: 5, name: '周五' },
        { value: 6, name: '周六' },
        { value: 7, name: '周日' }
      ],
      periodOptions: [
        { value: 1, timeRange: '09:00-11:50', description: '上午' },
        { value: 2, timeRange: '12:30-15:20', description: '下午早' },
        { value: 3, timeRange: '15:30-18:20', description: '下午晚' },
        { value: 4, timeRange: '19:00-21:50', description: '晚上' }
      ]
    }
  },
  computed: {
    hasScheduleFilter() {
      return this.selectedDays.length > 0 || this.selectedPeriods.length > 0;
    }
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 并行加载课程和院系数据
        const [coursesRes, facultiesRes] = await Promise.all([
          courseService.getAllCourses(),
          facultyService.getFaculties()
        ]);
        
        this.courses = coursesRes.data;
        this.faculties = facultiesRes.data;
        
        // 加载所有课程的时间安排
        await this.loadAllSchedules();
        
        // 排序：有AI总结的在前，无AI总结的在后，每组内部按评论数降序
        this.filteredCourses = [...this.courses].sort((a, b) => {
          const aHasSummary = a.aiSummary ? 1 : 0;
          const bHasSummary = b.aiSummary ? 1 : 0;
          if (bHasSummary !== aHasSummary) {
            return bHasSummary - aHasSummary;
          }
          return (b.reviewCount || 0) - (a.reviewCount || 0);
        });
        
        this.loading = false;
      } catch (error) {
        console.error('加载数据失败:', error);
        this.loading = false;
      }
    },
    
    async loadAllSchedules() {
      // 并行加载所有课程的时间安排
      const schedulePromises = this.courses.map(course => 
        scheduleService.getCourseSchedules(course.id)
          .then(res => ({ courseId: course.id, schedules: res.data }))
          .catch(() => ({ courseId: course.id, schedules: [] }))
      );
      
      const results = await Promise.all(schedulePromises);
      
      // 构建课程ID到时间安排的映射
      this.courseSchedules = {};
      results.forEach(result => {
        this.courseSchedules[result.courseId] = result.schedules;
      });
    },
    
    filterCourses() {
      this.filteredCourses = this.courses.filter(course => {
        // 过滤院系
        const facultyMatch = !this.selectedFaculty || course.facultyId === this.selectedFaculty;
        
        // 过滤课程类型
        const typeMatch = !this.selectedType || course.type === this.selectedType;
        
        // 过滤搜索词
        const searchMatch = !this.searchQuery || 
          course.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          course.code.toLowerCase().includes(this.searchQuery.toLowerCase());
          
        // 过滤上课时间
        const scheduleMatch = this.matchSchedule(course.id);
          
        return facultyMatch && typeMatch && searchMatch && scheduleMatch;
      });
      
      // 排序：有AI总结的在前，无AI总结的在后，每组内部按评论数降序
      this.filteredCourses.sort((a, b) => {
        const aHasSummary = a.aiSummary ? 1 : 0;
        const bHasSummary = b.aiSummary ? 1 : 0;
        // 先按AI总结存在性排序（有总结的在前）
        if (bHasSummary !== aHasSummary) {
          return bHasSummary - aHasSummary;
        }
        // 再按评论数降序
        return (b.reviewCount || 0) - (a.reviewCount || 0);
      });
    },
    
    matchSchedule(courseId) {
      // 如果没有选择任何时间筛选，返回true
      if (this.selectedDays.length === 0 && this.selectedPeriods.length === 0) {
        return true;
      }
      
      const schedules = this.courseSchedules[courseId] || [];
      
      // 如果课程没有时间安排，根据是否有筛选条件决定
      if (schedules.length === 0) {
        return false; // 有筛选条件但课程没有时间安排，不匹配
      }
      
      // 检查课程的时间安排是否匹配所选条件
      return schedules.some(schedule => {
        const dayMatch = this.selectedDays.length === 0 || this.selectedDays.includes(schedule.dayOfWeek);
        const periodMatch = this.selectedPeriods.length === 0 || this.selectedPeriods.includes(schedule.timePeriod);
        return dayMatch && periodMatch;
      });
    },
    
    clearScheduleFilter() {
      this.selectedDays = [];
      this.selectedPeriods = [];
      this.filterCourses();
    },
    
    viewCourseDetails(courseId) {
      this.$router.push(`/courses/${courseId}`);
    },
    
    truncate(text, length) {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    }
  }
}
</script>

<style scoped>
.courses-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  margin: 0;
  color: #333;
}

.btn-back {
  display: inline-block;
  background-color: #0066cc;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #0055aa;
}

.filters {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
}

.search-box {
  flex: 2; /* 搜索框占2/4的宽度 */
}

.faculty-filter, .type-filter {
  flex: 1; /* 院系和类型过滤器各占1/4的宽度 */
}

.search-box input,
.faculty-filter select, 
.type-filter select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

/* 时间筛选样式 */
.schedule-filter {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-title {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.filter-title i {
  color: #0066cc;
  margin-right: 8px;
}

.btn-clear {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 5px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-clear:hover {
  background-color: #e0e0e0;
  border-color: #bbb;
}

.btn-clear i {
  margin-right: 5px;
}

.schedule-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.day-options, .period-options {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.option-label {
  font-size: 14px;
  color: #555;
  min-width: 60px;
  padding-top: 4px;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.checkbox-item input[type="checkbox"] {
  margin-right: 5px;
  cursor: pointer;
}

.checkbox-text {
  font-size: 14px;
  color: #333;
  padding: 4px 8px;
  background-color: #f8f8f8;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.checkbox-item input[type="checkbox"]:checked + .checkbox-text {
  background-color: #e3f2fd;
  border-color: #0066cc;
  color: #0066cc;
}

.checkbox-item:hover .checkbox-text {
  background-color: #f0f0f0;
}

.courses-list {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  background-color: #fff;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.course-code {
  font-weight: bold;
  color: #555;
}

.course-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.course-type.compulsory {
  background-color: #e53935;
}

.course-type.elective {
  background-color: #43a047;
}

.course-info {
  padding: 15px;
}

.course-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.course-faculty {
  color: #0066cc;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-teacher {
  color: #555;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-credits {
  color: #555;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-description {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
  margin: 10px 0 0 0;
}

.no-results, .loading {
  text-align: center;
  padding: 30px;
  color: #666;
}
</style> 