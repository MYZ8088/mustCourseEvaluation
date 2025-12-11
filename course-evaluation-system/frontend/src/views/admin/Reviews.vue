<template>
  <div>
    <div class="admin-header">
      <h1>评价管理</h1>
      <div class="filter-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索评价内容..." 
            @input="handleSearch"
          />
        </div>
        
        <button 
          class="btn-danger" 
          :disabled="selectedReviews.length === 0" 
          @click="confirmBatchDelete"
        >
          批量删除 ({{ selectedReviews.length }})
        </button>
      </div>
    </div>
    
    <div class="admin-card">
      <table v-if="!loading" class="data-table">
        <thead>
          <tr>
            <th style="width: 40px; min-width: 40px;">
              <input 
                type="checkbox" 
                :checked="isAllSelected" 
                @change="toggleSelectAll"
              />
            </th>
            <th style="width: 60px; min-width: 60px;">ID</th>
            <th style="min-width: 100px;">内容</th>
            <th style="width: 100px; min-width: 100px;">评分</th>
            <th style="width: 120px; min-width: 100px;">用户</th>
            <th style="width: 150px; min-width: 120px;">课程</th>
            <th style="width: 120px; min-width: 100px;">时间</th>
            <th style="width: 100px; min-width: 80px;">状态</th>
            <th style="width: 100px; min-width: 80px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="reviews.length === 0">
            <td colspan="9" class="no-data">暂无评价数据</td>
          </tr>
          <tr v-for="review in reviews" :key="review.id">
            <td>
              <input 
                type="checkbox" 
                :checked="selectedReviews.includes(review.id)" 
                @change="toggleSelect(review.id)"
              />
            </td>
            <td>{{ review.id }}</td>
            <td class="review-content">
              <span :class="{ 'anonymous': review.anonymous }">
                {{ review.content }}
              </span>
            </td>
            <td>
              <div class="rating-stars">
                <i v-for="n in 5" :key="n" 
                   :class="n <= Number(review.rating) ? 'fas fa-star' : 'far fa-star'"></i>
              </div>
            </td>
            <td>{{ review.anonymous ? '匿名用户' : review.username }}</td>
            <td>
              <router-link :to="`/courses/${review.courseId}`">
                {{ review.courseName }}
              </router-link>
            </td>
            <td>{{ formatDate(review.createdAt) }}</td>
            <td>
              <span class="status-badge status-approved">
                已发布
              </span>
            </td>
            <td class="actions">
              <div class="action-buttons">
                <button class="btn-delete" @click="confirmDelete(review)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-else class="loading">
        <p>加载中，请稍候...</p>
      </div>
      
      <div class="pagination-controls" v-if="!loading && reviews.length > 0">
        <div class="page-info">
          共 {{ totalItems }} 条记录，当前页 {{ currentPage }} / {{ totalPages }}
        </div>
        <div class="page-buttons">
          <button 
            :disabled="currentPage === 1"
            @click="goToPage(currentPage - 1)"
          >
            上一页
          </button>
          <button 
            v-for="page in displayedPages" 
            :key="page"
            :class="{ active: currentPage === page }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button 
            :disabled="currentPage === totalPages"
            @click="goToPage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
    
    <!-- 删除确认对话框 -->
    <div class="modal" v-if="showDeleteModal">
      <div class="modal-content delete-confirm">
        <div class="modal-header">
          <h2>删除确认</h2>
          <button class="close-btn" @click="cancelDelete">&times;</button>
        </div>
        <div class="modal-body">
          <p v-if="reviewToDelete">
            确定要删除这条评价吗？此操作不可恢复。
          </p>
          <p v-else>
            确定要删除选中的 {{ selectedReviews.length }} 条评价吗？此操作不可恢复。
          </p>
          
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="cancelDelete">取消</button>
            <button type="button" class="btn-delete" @click="executeDelete">
              确认删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import reviewService from '@/services/review.service'

export default {
  name: 'AdminReviews',
  data() {
    return {
      loading: true,
      reviews: [],
      
      // 筛选条件
      searchQuery: '',
      
      // 分页
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      
      // 选择和删除
      selectedReviews: [],
      showDeleteModal: false,
      reviewToDelete: null,
      
      // 搜索防抖
      searchTimeout: null
    }
  },
  computed: {
    // 计算总页数
    totalPages() {
      return Math.ceil(this.totalItems / this.pageSize)
    },
    
    // 分页显示逻辑
    displayedPages() {
      const pages = []
      const maxPagesToShow = 5
      
      let startPage = Math.max(1, this.currentPage - Math.floor(maxPagesToShow / 2))
      let endPage = startPage + maxPagesToShow - 1
      
      if (endPage > this.totalPages) {
        endPage = this.totalPages
        startPage = Math.max(1, endPage - maxPagesToShow + 1)
      }
      
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i)
      }
      
      return pages
    },
    
    // 是否全选
    isAllSelected() {
      return this.selectedReviews.length > 0 && this.selectedReviews.length === this.reviews.length
    }
  },
  created() {
    this.fetchReviews()
  },
  methods: {
    // 获取评价列表
    fetchReviews() {
      this.loading = true
      
      const params = {
        page: this.currentPage - 1,
        size: this.pageSize,
        query: this.searchQuery
      }
      
      reviewService.getAllReviews(params)
        .then(response => {
          this.reviews = response.data.content || response.data
          
          // 确保评分数据为数字类型
          this.reviews = this.reviews.map(review => ({
            ...review,
            rating: Number(review.rating)
          }));
          
          // 添加日志记录用于调试星级问题
          console.log('评价列表数据:', this.reviews);
          this.reviews.forEach(review => {
            console.log(`评价ID ${review.id} 的评分: ${review.rating}，类型: ${typeof review.rating}`);
          });
          
          this.totalItems = response.data.totalElements || this.reviews.length
          this.selectedReviews = []
          this.loading = false
        })
        .catch(error => {
          console.error('获取评价列表失败', error)
          this.loading = false
        })
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '-'
      
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    },
    
    // 翻页功能
    goToPage(page) {
      if (page < 1 || page > this.totalPages) return
      this.currentPage = page
      this.fetchReviews()
    },
    
    // 搜索处理（带防抖）
    handleSearch() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }
      
      this.searchTimeout = setTimeout(() => {
        this.currentPage = 1
        this.fetchReviews()
      }, 300)
    },
    
    // 选择功能
    toggleSelect(id) {
      const index = this.selectedReviews.indexOf(id)
      if (index === -1) {
        this.selectedReviews.push(id)
      } else {
        this.selectedReviews.splice(index, 1)
      }
    },
    
    // 全选/全不选
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedReviews = []
      } else {
        this.selectedReviews = this.reviews.map(review => review.id)
      }
    },
    
    // 确认删除单条评价
    confirmDelete(review) {
      this.reviewToDelete = review
      this.showDeleteModal = true
    },
    
    // 确认批量删除
    confirmBatchDelete() {
      this.reviewToDelete = null
      this.showDeleteModal = true
    },
    
    // 取消删除
    cancelDelete() {
      this.showDeleteModal = false
      this.reviewToDelete = null
    },
    
    // 执行删除操作
    executeDelete() {
      if (this.reviewToDelete) {
        // 单条删除
        reviewService.adminDeleteReview(this.reviewToDelete.id)
          .then(() => {
            this.reviews = this.reviews.filter(r => r.id !== this.reviewToDelete.id)
            this.totalItems--
            this.showDeleteModal = false
            this.reviewToDelete = null
            
            // 如果当前页已无数据且不是第一页，则返回上一页
            if (this.reviews.length === 0 && this.currentPage > 1) {
              this.goToPage(this.currentPage - 1)
            }
            
            this.$notify({
              type: 'success',
              title: '成功',
              message: '评价已删除'
            })
          })
          .catch(error => {
            console.error('删除评价失败', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '删除评价失败: ' + (error.response?.data?.message || error.message)
            })
            this.showDeleteModal = false
          })
      } else {
        // 批量删除
        reviewService.batchDeleteReviews(this.selectedReviews)
          .then(() => {
            this.reviews = this.reviews.filter(r => !this.selectedReviews.includes(r.id))
            this.totalItems -= this.selectedReviews.length
            this.selectedReviews = []
            this.showDeleteModal = false
            
            // 如果当前页已无数据且不是第一页，则返回上一页
            if (this.reviews.length === 0 && this.currentPage > 1) {
              this.goToPage(this.currentPage - 1)
            }
            
            this.$notify({
              type: 'success',
              title: '成功',
              message: '批量删除评价成功'
            })
          })
          .catch(error => {
            console.error('批量删除评价失败', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '批量删除评价失败: ' + (error.response?.data?.message || error.message)
            })
            this.showDeleteModal = false
          })
      }
    }
  }
}
</script>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

h1 {
  margin: 0 0 20px 0;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  width: 100%;
  align-items: center;
}

.search-box {
  flex: 1;
  margin-right: 0;
}

.search-box input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.select-filter select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 150px;
}

.btn-danger {
  background-color: #d32f2f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  margin-left: 15px;
  flex-shrink: 0;
}

.btn-danger:hover {
  background-color: #c62828;
}

.btn-danger:disabled {
  background-color: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
}

.admin-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
  overflow: hidden;
  height: 48px; /* 统一所有单元格高度 */
  box-sizing: border-box; /* 确保内边距不影响高度计算 */
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 500;
}

.review-content {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  position: relative;
  min-width: 80px; /* 最小宽度 */
}

.anonymous {
  font-style: italic;
  color: #666;
}

.rating-stars {
  color: #ffc107;
  white-space: nowrap;
  display: inline-block;
  line-height: 1; /* 确保行高一致 */
  min-width: 80px; /* 确保有足够的宽度显示5颗星 */
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  line-height: 1.2; /* 统一行高 */
  box-sizing: border-box; /* 确保padding不会增加总高度 */
}

.status-approved {
  background-color: #28a745;
  color: white;
}

.actions {
  display: flex;
  gap: 8px;
  white-space: nowrap;
  height: 100%; /* 确保充满单元格 */
  align-items: center; /* 垂直居中 */
}

.action-buttons {
  display: flex;
  gap: 5px;
  white-space: nowrap;
  height: 28px; /* 固定按钮高度 */
}

.btn-delete {
  background-color: #fbe9e7;
  color: #d32f2f;
  border: none;
  padding: 5px 8px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.85em;
  height: 28px; /* 确保所有按钮高度一致 */
  box-sizing: border-box; /* 确保padding不会增加高度 */
  display: inline-flex; /* 使用flex布局确保内容垂直居中 */
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
}

.btn-delete:hover {
  background-color: #ffccbc;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

.loading {
  text-align: center;
  padding: 30px;
  color: #6c757d;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.page-info {
  color: #6c757d;
}

.page-buttons {
  display: flex;
  gap: 5px;
}

.page-buttons button {
  min-width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  border-radius: 3px;
}

.page-buttons button:hover {
  background-color: #f5f5f5;
}

.page-buttons button.active {
  background-color: #1976d2;
  color: white;
  border-color: #1976d2;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 500px;
  max-width: 95%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.close-btn {
  border: none;
  background: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.delete-confirm p {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #616161;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-cancel:hover {
  background-color: #eeeeee;
}

.btn-submit {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-submit:hover {
  background-color: #1565c0;
}

/* 整合所有媒体查询 */
@media (max-width: 1200px) {
  .admin-card {
    overflow-x: auto; /* 在容器上添加水平滚动 */
    padding: 20px; /* 保持与原来一致的内边距 */
  }
  
  .data-table {
    min-width: 1000px; /* 确保表格有最小宽度 */
    white-space: nowrap; /* 防止内容换行导致的错位 */
    margin: 0; /* 确保表格没有外边距 */
  }
  
  /* 调整评分、状态和操作列的显示 */
  .rating-stars, .status-badge, .action-buttons {
    white-space: nowrap; /* 这些不应该换行 */
    display: inline-flex; /* 使用inline-flex保持一致的显示 */
    align-items: center;
  }
  
  /* 确保评分列显示完整 */
  th:nth-child(4), td:nth-child(4) {
    width: 100px;
    min-width: 100px;
  }
  
  /* 内容列可以在空间不足时被压缩 */
  th:nth-child(3), td:nth-child(3) {
    width: auto;
    min-width: 80px;
  }
}

@media (max-width: 768px) {
  .review-content {
    min-width: 80px; /* 在小屏幕上进一步减少内容列的最小宽度 */
  }
  
  /* 更严格的空间分配策略 */
  .data-table th, .data-table td {
    padding: 10px 8px; /* 减少内边距节省空间 */
  }
  
  /* 在最小屏幕上可以隐藏部分不太重要的列 */
  th:nth-child(6), td:nth-child(6),  /* 课程 */
  th:nth-child(7), td:nth-child(7) { /* 时间 */
    display: none;
  }
}
</style> 