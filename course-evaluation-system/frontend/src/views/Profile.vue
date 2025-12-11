<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人中心</h1>
      <router-link to="/" class="btn-back">
        <i class="fas fa-home"></i> 返回首页
      </router-link>
    </div>
    
    <div v-if="loading" class="loading-indicator">
      <p>加载中，请稍候...</p>
    </div>
    
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="fetchUserData" class="btn-retry">重试</button>
    </div>
    
    <div v-else class="profile-content">
      <!-- 用户基本信息卡片 -->
      <div class="user-info-card">
        <div class="user-header">
          <div class="avatar">
            <i class="fas fa-user-circle"></i>
          </div>
          <div class="user-details">
            <h2>{{ user.username }}</h2>
            <div class="user-meta">
              <span class="user-role" :class="getRoleClass(user.role)">
                {{ getRoleName(user.role) }}
              </span>
              <span v-if="user.studentId" class="student-id">学号: {{ user.studentId }}</span>
            </div>
          </div>
        </div>
        
        <div class="user-info-details">
          <div class="info-item">
            <span class="label"><i class="fas fa-envelope"></i> 邮箱:</span>
            <span class="value">{{ user.email }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fas fa-user"></i> 姓名:</span>
            <span class="value">{{ user.fullName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fas fa-clock"></i> 注册时间:</span>
            <span class="value">{{ user.createdAt ? formatDate(user.createdAt) : '未知' }}</span>
          </div>
        </div>
      </div>
      
      <!-- 我的评价历史 -->
      <div class="my-reviews">
        <h3>我的评价历史</h3>
        
        <div v-if="reviewsLoading" class="loading-indicator">
          <p>加载评价中...</p>
        </div>
        
        <div v-else-if="reviewsError" class="error-message">
          <p>{{ reviewsError }}</p>
          <button @click="fetchUserReviews" class="btn-retry">重试</button>
        </div>
        
        <div v-else-if="userReviews.length === 0" class="no-reviews">
          <p>您还没有发表过评价</p>
          <router-link to="/courses" class="btn-primary">
            浏览课程
          </router-link>
        </div>
        
        <div v-else class="reviews-list">
          <div v-for="review in userReviews" :key="review.id" class="review-card">
            <div class="review-status" :class="`status-${review.status.toLowerCase()}`">
              {{ getStatusText(review.status) }}
            </div>
            
            <div class="review-course">
              <router-link :to="`/courses/${review.courseId}`" class="course-link">
                {{ review.courseName }} ({{ review.courseCode }})
              </router-link>
            </div>
            
            <div class="review-rating">
              <i v-for="n in 5" :key="n" class="fas" 
                 :class="n <= review.rating ? 'fa-star' : 'far fa-star'"></i>
            </div>
            
            <div class="review-content">
              {{ review.content }}
            </div>
            
            <div class="review-footer">
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              <span v-if="review.anonymous" class="anonymous-badge">匿名</span>
              
              <div class="review-actions">
                <button v-if="canEditReview(review)" @click="editReview(review)" class="btn-edit-review">
                  <i class="fas fa-edit"></i> 编辑
                </button>
                <button v-if="canDeleteReview(review)" @click="confirmDeleteReview(review)" class="btn-delete-review">
                  <i class="fas fa-trash"></i> 删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 删除评价确认对话框 -->
    <div v-if="showDeleteConfirm" class="delete-confirm-modal">
      <div class="modal-content">
        <h4>确认删除</h4>
        <p>您确定要删除这条评价吗？此操作无法撤销。</p>
        <div class="modal-actions">
          <button @click="showDeleteConfirm = false" class="btn-cancel">取消</button>
          <button @click="deleteReview" class="btn-confirm-delete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import userService from '@/services/user.service'
import reviewService from '@/services/review.service'
import { mapGetters } from 'vuex'

export default {
  name: 'Profile',
  data() {
    return {
      loading: true,
      error: null,
      user: {},
      reviewsLoading: true,
      reviewsError: null,
      userReviews: [],
      showDeleteConfirm: false,
      reviewToDelete: null
    }
  },
  computed: {
    ...mapGetters({
      currentUser: 'auth/currentUser'
    })
  },
  methods: {
    fetchUserData() {
      this.loading = true
      this.error = null
      
      // 首先尝试用store中的currentUser
      if (this.currentUser && this.currentUser.id) {
        this.user = { ...this.currentUser }
        this.loading = false
        return;
      }
      
      // 否则尝试通过API获取
      userService.getCurrentUser()
        .then(response => {
          this.user = response.data
          this.loading = false
        })
        .catch(error => {
          console.error('获取用户数据失败:', error)
          this.error = '获取用户信息失败，请稍后再试'
          this.loading = false
        })
    },
    
    fetchUserReviews() {
      this.reviewsLoading = true
      this.reviewsError = null
      
      reviewService.getUserReviews(this.user.id)
        .then(response => {
          this.userReviews = response.data
          this.reviewsLoading = false
        })
        .catch(error => {
          console.error('获取用户评价失败:', error)
          this.reviewsError = '获取评价历史失败，请稍后再试'
          this.reviewsLoading = false
        })
    },
    
    getRoleName(role) {
      switch (role) {
        case 'ROLE_ADMIN': return '管理员'
        case 'ROLE_STUDENT': return '学生'
        default: return '用户'
      }
    },
    
    getRoleClass(role) {
      switch (role) {
        case 'ROLE_ADMIN': return 'admin'
        case 'ROLE_STUDENT': return 'student'
        default: return ''
      }
    },
    
    getStatusText(status) {
      switch (status) {
        case 'APPROVED': return '已发布'
        default: return ''
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    canEditReview(review) {
      return review.status !== 'APPROVED'
    },
    
    canDeleteReview(review) {
      return true
    },
    
    // eslint-disable-next-line no-unused-vars
    editReview(review) {
      this.$router.push(`/courses/${review.courseId}?editReview=${review.id}`)
    },
    
    confirmDeleteReview(review) {
      this.reviewToDelete = review
      this.showDeleteConfirm = true
    },
    
    deleteReview() {
      if (!this.reviewToDelete) return
      
      reviewService.deleteReview(this.reviewToDelete.id)
        .then(() => {
          this.userReviews = this.userReviews.filter(r => r.id !== this.reviewToDelete.id)
          this.showDeleteConfirm = false
          this.reviewToDelete = null
        })
        .catch(error => {
          console.error('删除评价失败:', error)
          alert('删除评价失败，请稍后再试')
        })
    }
  },
  created() {
    this.fetchUserData()
  },
  watch: {
    user(newVal) {
      if (newVal && newVal.id) {
        this.fetchUserReviews()
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
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

.loading-indicator, .error-message {
  text-align: center;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.btn-retry {
  display: inline-block;
  background-color: #f1f1f1;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-top: 10px;
  cursor: pointer;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.user-info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  font-size: 64px;
  color: #0066cc;
  margin-right: 20px;
}

.user-details h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.user-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-role {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.user-role.admin {
  background-color: #dc3545;
}

.user-role.student {
  background-color: #28a745;
}

.student-id {
  color: #666;
  font-size: 14px;
}

.user-info-details {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-item .label {
  width: 100px;
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-cancel {
  background-color: #f1f1f1;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
}

.my-reviews {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.my-reviews h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.no-reviews {
  text-align: center;
  padding: 30px;
  color: #666;
}

.btn-primary {
  display: inline-block;
  background-color: #0066cc;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  margin-top: 10px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  position: relative;
}

.review-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.status-approved {
  background-color: #28a745;
}

.status-pending {
  background-color: #ffc107;
  color: #333;
}

.status-rejected {
  background-color: #dc3545;
}

.review-course {
  margin-bottom: 10px;
  font-weight: 500;
}

.course-link {
  color: #0066cc;
  text-decoration: none;
}

.course-link:hover {
  text-decoration: underline;
}

.review-rating {
  color: #ffc107;
  margin-bottom: 10px;
}

.review-content {
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #777;
  font-size: 14px;
}

.anonymous-badge {
  background-color: #f1f1f1;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 10px;
}

.review-actions {
  display: flex;
  gap: 10px;
}

.btn-edit-review, .btn-delete-review {
  background-color: transparent;
  border: none;
  color: #0066cc;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}

.btn-delete-review {
  color: #dc3545;
}

.delete-confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.modal-content h4 {
  margin-top: 0;
  color: #333;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-confirm-delete {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
</style> 