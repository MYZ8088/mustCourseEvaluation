<template>
  <div>
    <div class="admin-header">
      <h1>院系管理</h1>
      <button class="btn-primary" @click="showAddFacultyModal">添加院系</button>
    </div>
    
    <!-- 搜索 -->
    <div class="filters">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索院系名称..." 
          @input="filterFaculties"
        />
      </div>
    </div>
    
    <!-- 院系列表 -->
    <div class="admin-card">
      <table v-if="!loading" class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>简介</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredFaculties.length === 0">
            <td colspan="4" class="no-data">没有找到符合条件的院系</td>
          </tr>
          <tr v-for="faculty in filteredFaculties" :key="faculty.id">
            <td>{{ faculty.id }}</td>
            <td>{{ faculty.name }}</td>
            <td class="description-cell">{{ faculty.description || '-' }}</td>
            <td class="actions">
              <button class="btn-edit" @click="editFaculty(faculty)">编辑</button>
              <button class="btn-delete" @click="confirmDelete(faculty)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="loading">加载中，请稍候...</div>
    </div>
    
    <!-- 添加/编辑院系模态框 -->
    <div class="modal" v-if="showModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑院系' : '添加院系' }}</h2>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitFacultyForm">
            <div class="form-group">
              <label for="name">院系名称</label>
              <input type="text" id="name" v-model="formData.name" required>
            </div>
            
            <div class="form-group">
              <label for="description">院系简介</label>
              <textarea id="description" v-model="formData.description" rows="4"></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
              <button type="submit" class="btn-submit">{{ isEditing ? '保存修改' : '添加院系' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 删除确认模态框 -->
    <div class="modal" v-if="showDeleteModal">
      <div class="modal-content delete-confirm">
        <div class="modal-header">
          <h2>删除确认</h2>
          <button class="close-btn" @click="cancelDelete">&times;</button>
        </div>
        <div class="modal-body">
          <p>确定要删除院系 <strong>{{ facultyToDelete?.name }}</strong> 吗？</p>
          <p class="warning">此操作将删除与该院系关联的所有教师和课程，不可恢复！</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="cancelDelete">取消</button>
            <button type="button" class="btn-delete" @click="deleteFaculty">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import facultyService from '@/services/faculty.service'

export default {
  name: 'AdminFaculties',
  data() {
    return {
      loading: true,
      faculties: [],
      searchQuery: '',
      filteredFaculties: [],
      
      // 模态框相关
      showModal: false,
      isEditing: false,
      formData: this.getEmptyFormData(),
      
      // 删除确认
      showDeleteModal: false,
      facultyToDelete: null
    }
  },
  created() {
    this.loadFaculties()
  },
  methods: {
    getEmptyFormData() {
      return {
        id: null,
        name: '',
        description: ''
      }
    },
    
    loadFaculties() {
      this.loading = true
      facultyService.getFaculties()
        .then(response => {
          this.faculties = response.data
          this.filteredFaculties = [...this.faculties]
          this.loading = false
        })
        .catch(error => {
          console.error('获取院系列表失败:', error)
          this.loading = false
        })
    },
    
    filterFaculties() {
      const searchLower = this.searchQuery.toLowerCase().trim()
      
      if (!searchLower) {
        this.filteredFaculties = [...this.faculties]
        return
      }
      
      this.filteredFaculties = this.faculties.filter(faculty => 
        faculty.name.toLowerCase().includes(searchLower) ||
        (faculty.description && faculty.description.toLowerCase().includes(searchLower))
      )
    },
    
    showAddFacultyModal() {
      this.isEditing = false
      this.formData = this.getEmptyFormData()
      this.showModal = true
    },
    
    editFaculty(faculty) {
      this.isEditing = true
      this.formData = { ...faculty }
      this.showModal = true
    },
    
    closeModal() {
      this.showModal = false
    },
    
    submitFacultyForm() {
      if (this.isEditing) {
        // 编辑现有院系
        facultyService.updateFaculty(this.formData.id, this.formData)
          .then(() => {
            this.closeModal()
            this.loadFaculties() // 重新加载院系列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '院系信息已更新'
            })
          })
          .catch(error => {
            console.error('更新院系失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '更新院系失败: ' + (error.response?.data?.message || error.message)
            })
          })
      } else {
        // 创建新院系
        facultyService.createFaculty(this.formData)
          .then(() => {
            this.closeModal()
            this.loadFaculties() // 重新加载院系列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '院系已创建'
            })
          })
          .catch(error => {
            console.error('创建院系失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '创建院系失败: ' + (error.response?.data?.message || error.message)
            })
          })
      }
    },
    
    confirmDelete(faculty) {
      this.facultyToDelete = faculty
      this.showDeleteModal = true
    },
    
    cancelDelete() {
      this.facultyToDelete = null
      this.showDeleteModal = false
    },
    
    deleteFaculty() {
      if (!this.facultyToDelete) return
      
      facultyService.deleteFaculty(this.facultyToDelete.id)
        .then(() => {
          this.faculties = this.faculties.filter(faculty => faculty.id !== this.facultyToDelete.id)
          this.filterFaculties() // 更新筛选后的列表
          this.cancelDelete()
          this.$notify({
            type: 'success',
            title: '成功',
            message: '院系已删除'
          })
        })
        .catch(error => {
          console.error('删除院系失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '删除院系失败: ' + (error.response?.data?.message || error.message)
          })
        })
    }
  }
}
</script>

<style scoped>
h1 {
  margin-bottom: 30px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.filters {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
  box-sizing: border-box;
}

.search-box {
  flex: 1;
  width: 100%;
}

.search-box input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.data-table tr:hover {
  background-color: #f8f9fa;
}

.description-cell {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-data {
  text-align: center;
  padding: 30px;
  color: #666;
}

.loading {
  text-align: center;
  padding: 30px;
  color: #666;
}

.actions {
  display: flex;
  gap: 5px;
}

.actions button {
  padding: 5px 8px;
  border: none;
  border-radius: 3px;
  font-size: 0.85em;
  cursor: pointer;
}

.btn-edit {
  background-color: #e3f2fd;
  color: #1565c0;
}

.btn-edit:hover {
  background-color: #bbdefb;
}

.btn-delete {
  background-color: #fbe9e7;
  color: #d32f2f;
}

.btn-delete:hover {
  background-color: #ffccbc;
}

.btn-primary {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-primary:hover {
  background-color: #1565c0;
}

/* 模态框样式 */
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

.form-group {
  margin-bottom: 15px;
  width: 100%;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input[type="text"],
.form-group select {
  height: 40px;
}

.form-group textarea {
  height: 120px; /* 让院系简介的文本框更高 */
  resize: none; /* 禁止用户调整大小 */
  min-height: 80px;
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

.delete-confirm .warning {
  color: #d32f2f;
  font-weight: 500;
}
</style> 