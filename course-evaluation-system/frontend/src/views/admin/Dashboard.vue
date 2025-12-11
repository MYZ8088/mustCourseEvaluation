<template>
  <div>
    <h1>控制面板</h1>
    <div class="dashboard-stats">
      <div class="stat-card">
        <h3>用户总数</h3>
        <p class="stat-value">{{ stats.userCount || '0' }}</p>
      </div>
      <div class="stat-card">
        <h3>课程总数</h3>
        <p class="stat-value">{{ stats.courseCount || '0' }}</p>
      </div>
      <div class="stat-card">
        <h3>教师总数</h3>
        <p class="stat-value">{{ stats.teacherCount || '0' }}</p>
      </div>
      <div class="stat-card">
        <h3>评价总数</h3>
        <p class="stat-value">{{ stats.reviewCount || '0' }}</p>
      </div>
    </div>
    
    <div class="recent-activities">
      <h2>最近活动</h2>
      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>
      <div v-else-if="recentReviews.length === 0" class="empty-state">
        <p>暂无活动记录</p>
      </div>
      <div v-else class="activities-list">
        <div v-for="review in recentReviews" :key="review.id" class="activity-item">
          <div class="activity-icon">
            <i class="fas fa-comment"></i>
          </div>
          <div class="activity-content">
            <p class="activity-text">
              {{ review.anonymous ? '匿名用户' : review.username }} 对课程 
              <router-link :to="`/courses/${review.courseId}`">{{ review.courseName }}</router-link> 
              进行了评价
            </p>
            <p class="activity-time">{{ formatDate(review.createdAt) }}</p>
          </div>
          <div class="activity-status status-approved">
            已发布
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import userService from '@/services/user.service'
import courseService from '@/services/course.service'
import reviewService from '@/services/review.service'
import teacherService from '@/services/teacher.service'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      loading: true,
      stats: {
        userCount: 0,
        courseCount: 0,
        teacherCount: 0,
        reviewCount: 0
      },
      recentReviews: []
    }
  },
  created() {
    this.fetchStats()
    this.fetchRecentReviews()
  },
  methods: {
    // 获取统计数据
    fetchStats() {
      // 获取用户总数
      userService.getUsers()
        .then(response => {
          this.stats.userCount = response.data.length
        })
        .catch(error => {
          console.error('获取用户统计失败:', error)
        })
      
      // 获取课程总数
      courseService.getAllCourses()
        .then(response => {
          this.stats.courseCount = response.data.length
        })
        .catch(error => {
          console.error('获取课程统计失败:', error)
        })
      
      // 获取教师总数
      teacherService.getTeachers()
        .then(response => {
          this.stats.teacherCount = response.data.length
        })
        .catch(error => {
          console.error('获取教师统计失败:', error)
        })
      
      // 获取评价总数
      reviewService.getAllReviews()
        .then(response => {
          this.stats.reviewCount = response.data.length || response.data.totalElements || 0
        })
        .catch(error => {
          console.error('获取评价统计失败:', error)
        })
    },
    
    // 获取最近评价
    fetchRecentReviews() {
      this.loading = true
      
      // 获取最近10条评价
      const params = {
        page: 0,
        size: 10,
        sort: 'createdAt,desc'
      }
      
      reviewService.getAllReviews(params)
        .then(response => {
          this.recentReviews = response.data.content || response.data
          this.loading = false
        })
        .catch(error => {
          console.error('获取最近评价失败:', error)
          this.loading = false
        })
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      
      // 计算距今时间
      const now = new Date()
      const diffMs = now - date
      const diffSec = Math.floor(diffMs / 1000)
      const diffMin = Math.floor(diffSec / 60)
      const diffHour = Math.floor(diffMin / 60)
      const diffDay = Math.floor(diffHour / 24)
      
      if (diffDay > 0) {
        return `${diffDay}天前`
      } else if (diffHour > 0) {
        return `${diffHour}小时前`
      } else if (diffMin > 0) {
        return `${diffMin}分钟前`
      } else {
        return '刚刚'
      }
    },
    
    // 翻译评价状态
    translateStatus(status) {
      return '已发布';
    },
    
    // 获取状态CSS类
    getStatusClass(status) {
      return 'status-approved';
    }
  }
}
</script>

<style scoped>
h1 {
  margin-bottom: 30px;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.stat-card h3 {
  margin-bottom: 10px;
  color: #666;
  font-size: 1rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #4a6bff;
  margin: 0;
}

.recent-activities {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.recent-activities h2 {
  margin-bottom: 20px;
  font-size: 1.5rem;
  color: #333;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 30px;
  color: #666;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: background-color 0.3s;
}

.activity-item:hover {
  background-color: #f0f2f5;
}

.activity-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e6f0ff;
  border-radius: 50%;
  margin-right: 15px;
  color: #1a73e8;
}

.activity-content {
  flex: 1;
}

.activity-text {
  margin: 0 0 5px 0;
  color: #333;
}

.activity-text a {
  color: #1a73e8;
  text-decoration: none;
}

.activity-text a:hover {
  text-decoration: underline;
}

.activity-time {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
}

.activity-status {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-approved {
  background-color: #e6f4ea;
  color: #137333;
}

.status-pending {
  background-color: #fef7e0;
  color: #b06000;
}

.status-rejected {
  background-color: #fce8e6;
  color: #c5221f;
}
</style> 