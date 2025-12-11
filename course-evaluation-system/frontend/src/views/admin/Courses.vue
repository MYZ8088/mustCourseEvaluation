<template>
  <div>
    <div class="admin-header">
      <h1>课程管理</h1>
      <button class="btn-primary" @click="showAddCourseModal">添加课程</button>
    </div>
    
    <!-- 搜索和过滤 -->
    <div class="filters">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索课程名称或代码..." 
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
    </div>
    
    <!-- 课程列表表格 -->
    <div class="admin-card">
      <table v-if="!loading" class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>代码</th>
            <th>名称</th>
            <th>学分</th>
            <th>类型</th>
            <th>教师</th>
            <th>院系</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredCourses.length === 0">
            <td colspan="8" class="no-data">没有找到符合条件的课程</td>
          </tr>
          <tr v-for="course in filteredCourses" :key="course.id">
            <td>{{ course.id }}</td>
            <td>{{ course.code }}</td>
            <td>{{ course.name }}</td>
            <td>{{ course.credits }}</td>
            <td>{{ translateCourseType(course.type) }}</td>
            <td>{{ course.teacherName || '-' }}</td>
            <td>{{ course.facultyName }}</td>
            <td class="actions">
              <button class="btn-view" @click="viewCourse(course)">查看</button>
              <button class="btn-edit" @click="editCourse(course)">编辑</button>
              <button class="btn-delete" @click="confirmDelete(course)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="loading">加载中，请稍候...</div>
    </div>
    
    <!-- 添加/编辑课程模态框 -->
    <div class="modal" v-if="showModal">
      <div class="modal-content course-modal">
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑课程' : '添加课程' }}</h2>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitCourseForm">
            <div class="form-row">
              <div class="form-group">
                <label for="code" class="required">课程代码</label>
                <input type="text" id="code" v-model="formData.code" required>
              </div>
              
              <div class="form-group">
                <label for="name" class="required">课程名称</label>
                <input type="text" id="name" v-model="formData.name" required>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="credits" class="required">学分</label>
                <input type="number" id="credits" v-model="formData.credits" min="0" step="0.5" required>
              </div>
              
              <div class="form-group">
                <label for="type" class="required">课程类型</label>
                <select id="type" v-model="formData.type" required>
                  <option value="COMPULSORY">必修课</option>
                  <option value="ELECTIVE">选修课</option>
                </select>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="faculty" class="required">所属院系</label>
                <select id="faculty" v-model="formData.facultyId" required @change="loadTeachersByFaculty">
                  <option value="">请选择院系</option>
                  <option v-for="faculty in faculties" :key="faculty.id" :value="faculty.id">
                    {{ faculty.name }}
                  </option>
                </select>
              </div>
              
              <div class="form-group">
                <label for="teacher">授课教师</label>
                <select id="teacher" v-model="formData.teacherId">
                  <option value="">请选择教师</option>
                  <option v-for="teacher in availableTeachers" :key="teacher.id" :value="teacher.id">
                    {{ teacher.name }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-group">
              <label for="description">课程描述</label>
              <textarea id="description" v-model="formData.description" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label for="syllabus">教学大纲</label>
              <textarea id="syllabus" v-model="formData.syllabus" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label for="assessment">考核方式</label>
              <textarea id="assessment" v-model="formData.assessmentMethods" rows="3"></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
              <button type="submit" class="btn-submit">{{ isEditing ? '保存修改' : '添加课程' }}</button>
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
          <p>确定要删除课程 <strong>{{ courseToDelete?.name }}</strong> ({{ courseToDelete?.code }}) 吗？</p>
          <p class="warning">此操作将删除该课程的所有评价，不可恢复！</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="cancelDelete">取消</button>
            <button type="button" class="btn-delete" @click="deleteCourse">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import courseService from '@/services/course.service'
import facultyService from '@/services/faculty.service'
import teacherService from '@/services/teacher.service'

export default {
  name: 'AdminCourses',
  data() {
    return {
      loading: true,
      courses: [],
      faculties: [],
      teachers: [],
      availableTeachers: [],
      searchQuery: '',
      selectedFaculty: null,
      filteredCourses: [],
      
      // 模态框相关
      showModal: false,
      isEditing: false,
      formData: this.getEmptyFormData(),
      
      // 删除确认
      showDeleteModal: false,
      courseToDelete: null
    }
  },
  created() {
    this.loadCourses()
    this.loadFaculties()
    this.loadTeachers()
  },
  methods: {
    getEmptyFormData() {
      return {
        id: null,
        code: '',
        name: '',
        credits: 3,
        type: 'COMPULSORY',
        description: '',
        syllabus: '',
        assessmentMethods: '',
        facultyId: '',
        teacherId: ''
      }
    },
    
    loadCourses() {
      this.loading = true
      courseService.getAllCourses()
        .then(response => {
          this.courses = response.data
          this.filteredCourses = [...this.courses]
          this.loading = false
        })
        .catch(error => {
          console.error('获取课程列表失败:', error)
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
    
    loadTeachers() {
      teacherService.getTeachers()
        .then(response => {
          this.teachers = response.data
        })
        .catch(error => {
          console.error('获取教师列表失败:', error)
        })
    },
    
    loadTeachersByFaculty() {
      if (!this.formData.facultyId) {
        this.availableTeachers = []
        return
      }
      
      this.availableTeachers = this.teachers.filter(teacher => 
        teacher.facultyId === this.formData.facultyId
      )
    },
    
    filterCourses() {
      const searchLower = this.searchQuery.toLowerCase().trim()
      
      this.filteredCourses = this.courses.filter(course => {
        // 过滤院系
        const facultyMatch = !this.selectedFaculty || course.facultyId === this.selectedFaculty
        
        // 过滤搜索词
        const searchMatch = !searchLower || 
          course.name.toLowerCase().includes(searchLower) || 
          course.code.toLowerCase().includes(searchLower) ||
          (course.description && course.description.toLowerCase().includes(searchLower))
        
        return facultyMatch && searchMatch
      })
    },
    
    translateCourseType(type) {
      const typeMap = {
        'COMPULSORY': '必修课',
        'ELECTIVE': '选修课',
        'REQUIRED': '必修课'
      }
      return typeMap[type] || type
    },
    
    viewCourse(course) {
      // 跳转到课程详情页
      this.$router.push(`/courses/${course.id}`)
    },
    
    showAddCourseModal() {
      this.isEditing = false
      this.formData = this.getEmptyFormData()
      this.availableTeachers = []
      this.showModal = true
    },
    
    editCourse(course) {
      this.isEditing = true
      // 复制课程数据到表单
      this.formData = { ...course }
      
      // 加载对应院系的教师列表
      if (course.facultyId) {
        this.availableTeachers = this.teachers.filter(teacher => 
          teacher.facultyId === course.facultyId
        )
      }
      
      this.showModal = true
    },
    
    closeModal() {
      this.showModal = false
    },
    
    submitCourseForm() {
      // 确保学分是数字类型
      this.formData.credits = Number(this.formData.credits)
      
      if (this.isEditing) {
        // 编辑现有课程
        courseService.updateCourse(this.formData.id, this.formData)
          .then(() => {
            this.closeModal()
            this.loadCourses() // 重新加载课程列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '课程信息已更新'
            })
          })
          .catch(error => {
            console.error('更新课程失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '更新课程失败: ' + (error.response?.data?.message || error.message)
            })
          })
      } else {
        // 创建新课程
        courseService.createCourse(this.formData)
          .then(() => {
            this.closeModal()
            this.loadCourses() // 重新加载课程列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '课程已创建'
            })
          })
          .catch(error => {
            console.error('创建课程失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '创建课程失败: ' + (error.response?.data?.message || error.message)
            })
          })
      }
    },
    
    confirmDelete(course) {
      this.courseToDelete = course
      this.showDeleteModal = true
    },
    
    cancelDelete() {
      this.courseToDelete = null
      this.showDeleteModal = false
    },
    
    deleteCourse() {
      if (!this.courseToDelete) return
      
      courseService.deleteCourse(this.courseToDelete.id)
        .then(() => {
          this.courses = this.courses.filter(course => course.id !== this.courseToDelete.id)
          this.filterCourses() // 更新筛选后的列表
          this.cancelDelete()
          this.$notify({
            type: 'success',
            title: '成功',
            message: '课程已删除'
          })
        })
        .catch(error => {
          console.error('删除课程失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '删除课程失败: ' + (error.response?.data?.message || error.message)
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
  padding: 0;
}

.search-box {
  flex: 3; /* 搜索框占3份宽度 */
}

.faculty-filter {
  flex: 1; /* 院系选择占1份宽度 */
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

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.data-table tr:hover {
  background-color: #f8f9fa;
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

.btn-view {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.btn-view:hover {
  background-color: #c8e6c9;
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
  width: 600px;
  max-width: 95%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: modal-appear 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

.course-modal {
  width: 600px;
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

.modal-body::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
  width: 0;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  flex: 1;
  min-width: 0; /* 防止flex子项溢出 */
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
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

.form-group textarea {
  min-height: 80px;
  height: 120px;
  resize: none;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  max-height: 100px; /* 减小文本框高度 */
}

/* 添加特殊样式用于描述、大纲和考核方式 */
#description, #syllabus, #assessment {
  height: 80px; /* 进一步减小这三个特定文本框的高度 */
  max-height: 80px;
}

.form-group textarea::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
  width: 0;
}

.form-group textarea::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.form-group textarea::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.form-group textarea::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 隐藏滚动条上下箭头 */
.form-group textarea::-webkit-scrollbar-button {
  display: none;
  height: 0;
  width: 0;
}

/* 确保模态框滚动条也是细的 */
.modal-body::-webkit-scrollbar {
  width: 4px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.modal-body::-webkit-scrollbar-button {
  display: none;
  height: 0;
  width: 0;
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

.delete-confirm .warning {
  color: #d32f2f;
  font-weight: 500;
}

/* 添加必填字段标记 */
label.required::after {
  content: " *";
  color: #f56c6c;
  margin-left: 4px;
}
</style> 