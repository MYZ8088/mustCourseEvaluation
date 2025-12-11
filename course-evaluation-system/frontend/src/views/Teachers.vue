<template>
  <div class="teachers-container">
    <div class="page-header">
      <h1>教师列表</h1>
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
          placeholder="搜索教师姓名..." 
          @input="filterTeachers"
        />
      </div>
      
      <div class="faculty-filter">
        <select v-model="selectedFaculty" @change="filterTeachers">
          <option :value="null">所有院系</option>
          <option v-for="faculty in faculties" :key="faculty.id" :value="faculty.id">
            {{ faculty.name }}
          </option>
        </select>
      </div>
    </div>
    
    <!-- 教师列表 -->
    <div class="teachers-list" v-if="!loading && !error">
      <div v-if="filteredTeachers.length === 0" class="no-results">
        <p>没有找到符合条件的教师</p>
      </div>
      
      <div v-else class="teachers-grid">
        <div 
          v-for="teacher in filteredTeachers" 
          :key="teacher.id" 
          class="teacher-card"
          @click="viewTeacherDetails(teacher.id)"
        >
          <div class="teacher-avatar">
            <div class="avatar-placeholder">
              {{ teacher.name.substring(0, 1) }}
            </div>
          </div>
            <div class="teacher-info">
            <h3>{{ teacher.name }}</h3>
            <p class="teacher-title">{{ teacher.title || '讲师' }}</p>
            <p class="teacher-faculty">{{ teacher.facultyName }}</p>
            <div class="teacher-courses-badge" v-if="teacher.courseCount > 0">
              <i class="fas fa-book"></i> {{ teacher.courseCount }} 门课程
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadData" class="btn-retry">重试</button>
    </div>
    
    <div v-else class="loading">
      <p>加载中，请稍候...</p>
    </div>
  </div>
</template>

<script>
import teacherService from '@/services/teacher.service'
import facultyService from '@/services/faculty.service'

export default {
  name: 'Teachers',
  data() {
    return {
      loading: true,
      error: null,
      teachers: [],
      faculties: [],
      searchQuery: '',
      selectedFaculty: null,
      filteredTeachers: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      this.error = null
      try {
        // 并行加载教师和院系数据（优化：减少等待时间）
        const [teachersRes, facultiesRes] = await Promise.all([
          teacherService.getTeachers(),
          facultyService.getFaculties()
        ])
        
        this.teachers = teachersRes.data || []
        this.faculties = facultiesRes.data || []
        this.filteredTeachers = [...this.teachers]
        this.loading = false
      } catch (error) {
        console.error('加载数据失败:', error)
        this.error = error.message || '加载数据失败，请稍后重试'
        this.loading = false
      }
    },
    
    filterTeachers() {
      this.filteredTeachers = this.teachers.filter(teacher => {
        // 过滤院系
        const facultyMatch = !this.selectedFaculty || teacher.facultyId === this.selectedFaculty
        
        // 过滤搜索词
        const searchMatch = !this.searchQuery || 
          teacher.name.toLowerCase().includes(this.searchQuery.toLowerCase())
          
        return facultyMatch && searchMatch
      })
    },
    
    viewTeacherDetails(teacherId) {
      // 跳转到教师详情页面
      this.$router.push(`/teachers/${teacherId}`)
    }
  }
}
</script>

<style scoped>
.teachers-container {
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
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box input,
.faculty-filter select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 100%;
}

.faculty-filter {
  min-width: 200px;
  margin-left: 15px;
}

.teachers-list {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.teachers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.teacher-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.teacher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.teacher-avatar {
  height: 150px;
  overflow: hidden;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #0066cc;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  font-weight: bold;
}

.teacher-info {
  padding: 15px;
}

.teacher-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.teacher-title {
  color: #666;
  margin: 0 0 5px 0;
  font-size: 14px;
}

.teacher-faculty {
  color: #0066cc;
  font-size: 14px;
  margin: 0;
}

.no-results {
  padding: 30px;
  text-align: center;
  color: #666;
}

.loading {
  text-align: center;
  padding: 30px;
  color: #666;
}

.error {
  text-align: center;
  padding: 30px;
  color: #d93025;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.btn-retry {
  margin-top: 15px;
  padding: 8px 20px;
  background-color: #0066cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-retry:hover {
  background-color: #0055aa;
}

.teacher-courses-badge {
  display: inline-block;
  background-color: #e8f4ff;
  color: #0066cc;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-top: 8px;
}

.teacher-courses-badge i {
  margin-right: 4px;
}
</style> 