<template>
  <div>
    <div class="admin-header">
      <h1>教师管理</h1>
      <button class="btn-primary" @click="showAddTeacherModal">添加教师</button>
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
    
    <!-- 教师列表表格 -->
    <div class="admin-card">
      <table v-if="!loading" class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>职称</th>
            <th>电子邮件</th>
            <th>院系</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredTeachers.length === 0">
            <td colspan="6" class="no-data">没有找到符合条件的教师</td>
          </tr>
          <tr v-for="teacher in filteredTeachers" :key="teacher.id">
            <td>{{ teacher.id }}</td>
            <td>{{ teacher.name }}</td>
            <td>{{ teacher.title || '-' }}</td>
            <td>{{ teacher.email || '-' }}</td>
            <td>{{ teacher.facultyName }}</td>
            <td class="actions">
              <button class="btn-edit" @click="editTeacher(teacher)">编辑</button>
              <button class="btn-delete" @click="confirmDelete(teacher)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="loading">加载中，请稍候...</div>
    </div>
    
    <!-- 教师添加/编辑模态框 -->
    <div class="modal" v-if="showModal">
      <div class="modal-content teacher-modal">
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑教师信息' : '添加新教师' }}</h2>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-container">
            <div class="form-row">
              <div class="form-group">
                <label for="teacherName">姓名 <span class="required">*</span></label>
                <input 
                  id="teacherName" 
                  type="text" 
                  v-model="formData.name" 
                  placeholder="请输入教师姓名"
                  autocomplete="off"
                  :class="{ error: formErrors.name }"
                />
                <span v-if="formErrors.name" class="error-message">{{ formErrors.name }}</span>
              </div>
              <div class="form-group">
                <label for="teacherTitle">职称</label>
                <input 
                  id="teacherTitle" 
                  type="text" 
                  v-model="formData.title" 
                  placeholder="请输入教师职称"
                  autocomplete="off"
                />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="teacherFaculty">所属院系 <span class="required">*</span></label>
                <select 
                  id="teacherFaculty" 
                  v-model="formData.facultyId"
                  :class="{ error: formErrors.facultyId }"
                >
                  <option value="">请选择院系</option>
                  <option 
                    v-for="faculty in faculties" 
                    :key="faculty.id" 
                    :value="faculty.id"
                  >
                    {{ faculty.name }}
                  </option>
                </select>
                <span v-if="formErrors.facultyId" class="error-message">{{ formErrors.facultyId }}</span>
              </div>
              <div class="form-group">
                <label for="teacherEmail">邮箱</label>
                <input 
                  id="teacherEmail" 
                  type="email" 
                  v-model="formData.email" 
                  placeholder="请输入教师邮箱"
                  autocomplete="off"
                  :class="{ error: formErrors.email }"
                />
                <span v-if="formErrors.email" class="error-message">{{ formErrors.email }}</span>
              </div>
            </div>
            
            <div class="form-group">
              <label for="teacherBio">简介</label>
              <textarea 
                id="teacherBio" 
                v-model="formData.bio" 
                placeholder="请输入教师简介"
                rows="3"
              ></textarea>
            </div>
          </div>
          
          <div class="form-actions">
            <button class="btn-cancel" @click="closeModal">取消</button>
            <button class="btn-submit" @click="submitTeacherForm">保存</button>
          </div>
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
          <p>确定要删除教师 <strong>{{ teacherToDelete?.name }}</strong> 吗？</p>
          <p class="warning">此操作不可逆，请谨慎操作！</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="cancelDelete">取消</button>
            <button type="button" class="btn-delete" @click="deleteTeacher">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import teacherService from '@/services/teacher.service'
import facultyService from '@/services/faculty.service'

export default {
  name: 'AdminTeachers',
  data() {
    return {
      loading: true,
      teachers: [],
      faculties: [],
      searchQuery: '',
      selectedFaculty: null,
      filteredTeachers: [],
      
      // 模态框相关
      showModal: false,
      isEditing: false,
      formData: this.getEmptyFormData(),
      formErrors: {},
      
      // 删除确认
      showDeleteModal: false,
      teacherToDelete: null
    }
  },
  created() {
    this.loadTeachers()
    this.loadFaculties()
  },
  methods: {
    getEmptyFormData() {
      return {
        id: null,
        name: '',
        title: '',
        email: '',
        bio: '',
        facultyId: ''
      }
    },
    
    loadTeachers() {
      this.loading = true
      teacherService.getTeachers()
        .then(response => {
          this.teachers = response.data
          this.filteredTeachers = [...this.teachers]
          this.loading = false
        })
        .catch(error => {
          console.error('获取教师列表失败:', error)
          this.loading = false
        })
    },
    
    loadFaculties() {
      facultyService.getFaculties()
        .then(response => {
          this.faculties = response.data
        })
        .catch(error => {
          console.error('获取院系列表失败:', error)
        })
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
    
    showAddTeacherModal() {
      this.isEditing = false
      this.formData = this.getEmptyFormData()
      this.showModal = true
    },
    
    editTeacher(teacher) {
      this.isEditing = true
      this.formData = { ...teacher }
      this.showModal = true
    },
    
    closeModal() {
      this.showModal = false
      this.formErrors = {}
    },
    
    validateForm() {
      const errors = {}
      
      if (!this.formData.name.trim()) {
        errors.name = '请输入教师姓名'
      }
      
      if (!this.formData.facultyId) {
        errors.facultyId = '请选择所属院系'
      }
      
      if (this.formData.email && !/^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/.test(this.formData.email)) {
        errors.email = '请输入有效的邮箱地址'
      }
      
      this.formErrors = errors
      return Object.keys(errors).length === 0
    },
    
    submitTeacherForm() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }
      
      const apiCall = this.isEditing
        ? teacherService.updateTeacher(this.formData.id, this.formData)
        : teacherService.createTeacher(this.formData)
        
      apiCall.then(() => {
          this.closeModal()
          this.loadTeachers() // 重新加载数据
          this.$notify({
            type: 'success',
            title: '成功',
            message: this.isEditing ? '教师信息已更新' : '教师已创建'
          })
        })
        .catch(error => {
          console.error('保存教师信息失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '保存失败: ' + (error.response?.data?.message || error.message)
          })
        })
    },
    
    confirmDelete(teacher) {
      this.teacherToDelete = teacher
      this.showDeleteModal = true
    },
    
    cancelDelete() {
      this.showDeleteModal = false
      this.teacherToDelete = null
    },
    
    deleteTeacher() {
      teacherService.deleteTeacher(this.teacherToDelete.id)
        .then(() => {
          this.showDeleteModal = false
          this.teacherToDelete = null
          this.loadTeachers() // 重新加载数据
          this.$notify({
            type: 'success',
            title: '成功',
            message: '教师已删除'
          })
        })
        .catch(error => {
          console.error('删除教师失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '删除失败: ' + (error.response?.data?.message || error.message)
          })
        })
    }
  }
}
</script>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  margin-bottom: 30px;
}

.filters {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
}

.search-box {
  flex: 3;
}

.faculty-filter {
  flex: 1;
}

.search-box input,
.faculty-filter select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  height: 40px;
  box-sizing: border-box;
}

.admin-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

/* 表格样式 */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.data-table tbody tr:hover {
  background-color: #f5f5f5;
}

.actions {
  white-space: nowrap;
}

.actions button {
  margin-right: 5px;
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

/* 按钮样式 */
.btn-primary {
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-weight: 500;
}

.btn-primary:hover {
  background-color: #1565c0;
}

.btn-edit {
  background-color: #e3f2fd;
  color: #1565c0;
  border: none;
  border-radius: 3px;
  padding: 5px 8px;
  cursor: pointer;
  font-size: 0.85em;
}

.btn-edit:hover {
  background-color: #bbdefb;
}

.btn-delete {
  background-color: #fbe9e7;
  color: #d32f2f;
  border: none;
  border-radius: 3px;
  padding: 5px 8px;
  cursor: pointer;
  font-size: 0.85em;
}

.btn-delete:hover {
  background-color: #ffccbc;
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
  width: 550px;
  max-width: 95%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: modal-appear 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

.teacher-modal {
  width: 550px;
  min-height: 400px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

@keyframes modal-appear {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.delete-confirm {
  width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #f8f9fa;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  flex-shrink: 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #333;
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
  overflow-y: auto;
  flex: 1;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group {
  flex: 1;
  min-width: 0; /* 防止flex子项溢出 */
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #333;
}

.form-group .required {
  color: #f44336;
  margin-left: 2px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 1rem;
  background-color: #fff;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input,
.form-group select {
  height: 40px;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #c0c4cc;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: #f44336;
  background-color: #fff8f8;
}

.error-message {
  color: #f44336;
  font-size: 0.85rem;
  margin-top: 4px;
  display: block;
}

.form-group select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23606266'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 8px center;
  background-size: 16px;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  padding-right: 30px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #616161;
  border: none;
  padding: 9px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-submit {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 9px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-submit:hover {
  background-color: #1565c0;
}

.warning {
  color: #dc3545;
  font-weight: 500;
}

.form-group textarea {
  min-height: 80px;
  height: 120px;
  resize: none;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.form-group textarea::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
  width: 0;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.modal-body::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
  width: 0;
}
</style> 