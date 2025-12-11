<template>
  <div>
    <div class="admin-header">
      <h1>用户管理</h1>
      <button class="btn-primary" @click="showAddUserModal">添加用户</button>
    </div>
    
    <!-- 搜索和过滤 -->
    <div class="filters">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索用户名或邮箱..." 
          @input="filterUsers"
        />
      </div>
      
      <div class="role-filter">
        <select v-model="selectedRole" @change="filterUsers">
          <option :value="null">所有角色</option>
          <option value="ROLE_ADMIN">管理员</option>
          <option value="ROLE_STUDENT">学生</option>
        </select>
      </div>
    </div>
    
    <!-- 用户列表表格 -->
    <div class="admin-card">
      <table v-if="!loading" class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>学号</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredUsers.length === 0">
            <td colspan="7" class="no-data">没有找到符合条件的用户</td>
          </tr>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.studentId || '-' }}</td>
            <td>
              <span :class="'role-badge ' + getRoleBadgeClass(user.role)">
                {{ translateRole(user.role) }}
              </span>
            </td>
            <td>
              <span :class="'status-badge ' + getUserStatusClass(user)">
                {{ getUserStatusText(user) }}
              </span>
            </td>
            <td class="actions">
              <button class="btn-edit" @click="editUser(user)">编辑</button>
              <button 
                v-if="user.active" 
                class="btn-comment-toggle"
                :class="user.canComment !== false ? 'btn-ban-comment' : 'btn-allow-comment'"
                @click="toggleCommentStatus(user)"
              >
                {{ user.canComment !== false ? '禁言' : '解除禁言' }}
              </button>
              <button 
                class="btn-toggle" 
                :class="user.active ? 'btn-deactivate' : 'btn-activate'"
                @click="toggleUserStatus(user)"
              >
                {{ user.active ? '停用' : '启用' }}
              </button>
              <button class="btn-delete" @click="confirmDelete(user)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="loading">加载中，请稍候...</div>
    </div>
    
    <!-- 添加/编辑用户模态框 -->
    <div class="modal" v-if="showModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑用户' : '添加用户' }}</h2>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-note">
            <span class="required-field">*</span> 表示必填项
          </div>
          <form @submit.prevent="submitUserForm">
            <div class="form-group">
              <label for="username">用户名 <span class="required-field">*</span></label>
              <input type="text" id="username" v-model="formData.username" required maxlength="20" @input="handleUsernameInput">
              <small v-if="usernameChecking" class="form-text text-muted">正在检查用户名是否可用...</small>
              <small v-if="usernameExists" class="form-text text-danger">该用户名已被使用</small>
              <small v-if="usernameAvailable" class="form-text text-success">该用户名可用</small>
              <small class="form-text text-muted">用户名长度限制：3-20个字符</small>
            </div>
            
            <div class="form-group">
              <label for="email">电子邮件 <span class="required-field">*</span></label>
              <input type="email" id="email" v-model="formData.email" required>
            </div>
            
            <div class="form-group" v-if="!isEditing">
              <label for="password">密码 <span class="required-field">*</span></label>
              <div class="password-input-container">
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  id="password" 
                  v-model="formData.password" 
                  @input="checkPassword"
                  :required="!isEditing" 
                  maxlength="32"
                >
                <button
                  v-if="formData.password"
                  type="button"
                  class="password-toggle-btn"
                  @click="showPassword = !showPassword"
                >
                  <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                    <path v-if="showPassword" d="M2 2l20 20" />
                  </svg>
                </button>
              </div>
              <small v-if="formData.password && !passwordValid" class="form-text text-danger">{{ passwordError }}</small>
              <small class="form-text text-muted">密码要求：长度8-32位，必须包含字母和数字，可包含特殊符号</small>
            </div>
            
            <div class="form-group-container">
              <div class="form-group form-group-left">
              <label for="studentId">学号</label>
              <input type="text" id="studentId" v-model="formData.studentId">
            </div>
            
              <div class="form-group form-group-right">
                <label for="role">角色 <span class="required-field">*</span></label>
              <select id="role" v-model="formData.role" required>
                <option value="ROLE_STUDENT">学生</option>
                <option value="ROLE_ADMIN">管理员</option>
              </select>
              </div>
            </div>
            
            <div class="form-group">
              <label for="fullName">姓名</label>
              <input type="text" id="fullName" v-model="formData.fullName">
            </div>
            
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
              <button type="submit" class="btn-submit">{{ isEditing ? '保存修改' : '添加用户' }}</button>
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
          <p>确定要删除用户 <strong>{{ userToDelete?.username }}</strong> 吗？</p>
          <p class="warning">此操作不可逆，请谨慎操作！</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="cancelDelete">取消</button>
            <button type="button" class="btn-delete" @click="deleteUser">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import userService from '@/services/user.service'
import authService from '@/services/auth.service'

export default {
  name: 'AdminUsers',
  data() {
    return {
      loading: true,
      users: [],
      searchQuery: '',
      selectedRole: null,
      filteredUsers: [],
      
      // 模态框相关
      showModal: false,
      isEditing: false,
      formData: this.getEmptyFormData(),
      
      // 删除确认
      showDeleteModal: false,
      userToDelete: null,
      
      // 密码显示控制
      showPassword: false,
      
      // 密码验证状态
      passwordValid: true,
      passwordError: '',
      
      // 用户名验证状态
      usernameChecking: false,
      usernameExists: false,
      usernameAvailable: false,
      usernameCheckTimeout: null
    }
  },
  created() {
    this.loadUsers()
  },
  beforeUnmount() {
    // 清理定时器
    if (this.usernameCheckTimeout) {
      clearTimeout(this.usernameCheckTimeout)
    }
  },
  methods: {
    getEmptyFormData() {
      return {
        id: null,
        username: '',
        email: '',
        password: '',
        studentId: '',
        fullName: '',
        role: 'ROLE_STUDENT',
        active: true,
        canComment: true
      }
    },
    
    loadUsers() {
      this.loading = true
      userService.getUsers()
        .then(response => {
          this.users = response.data
          this.filteredUsers = [...this.users]
          this.loading = false
        })
        .catch(error => {
          console.error('获取用户列表失败:', error)
          // 显示详细错误信息
          let errorMessage = '获取用户列表失败'
          if (error.response) {
            // 服务器响应了，但状态码不在2xx范围
            errorMessage += `: ${error.response.status} - ${error.response.statusText}`
            if (error.response.data && error.response.data.message) {
              errorMessage += ` - ${error.response.data.message}`
            }
          } else if (error.request) {
            // 请求已发出，但没有收到响应
            errorMessage += ': 没有收到服务器响应，请检查后端服务是否运行'
          } else {
            // 请求配置出错
            errorMessage += `: ${error.message}`
          }
          this.$notify({
            type: 'error',
            title: '错误',
            message: errorMessage
          })
          this.loading = false
        })
    },
    
    filterUsers() {
      const searchLower = this.searchQuery.toLowerCase().trim()
      
      this.filteredUsers = this.users.filter(user => {
        // 过滤角色
        const roleMatch = !this.selectedRole || user.role === this.selectedRole
        
        // 过滤搜索词
        const searchMatch = !searchLower || 
          user.username.toLowerCase().includes(searchLower) || 
          (user.email && user.email.toLowerCase().includes(searchLower)) ||
          (user.fullName && user.fullName.toLowerCase().includes(searchLower))
        
        return roleMatch && searchMatch
      })
    },
    
    translateRole(role) {
      const roleMap = {
        'ROLE_ADMIN': '管理员',
        'ROLE_STUDENT': '学生'
      }
      return roleMap[role] || role
    },
    
    getRoleBadgeClass(role) {
      const classMap = {
        'ROLE_ADMIN': 'role-admin',
        'ROLE_STUDENT': 'role-student'
      }
      return classMap[role] || ''
    },
    
    showAddUserModal() {
      this.isEditing = false
      this.formData = this.getEmptyFormData()
      this.usernameExists = false
      this.usernameAvailable = false
      this.showModal = true
    },
    
    editUser(user) {
      this.isEditing = true
      // 复制用户数据到表单
      this.formData = { ...user, password: '' }
      this.showModal = true
    },
    
    closeModal() {
      this.showModal = false
    },
    
    submitUserForm() {
      // 验证用户名长度
      if (this.formData.username.length < 3 || this.formData.username.length > 20) {
        this.$notify({
          type: 'error',
          title: '错误',
          message: '用户名长度必须在3-20个字符之间'
        })
        return
      }
      
      // 验证用户名是否可用（仅在创建新用户时）
      if (!this.isEditing && this.usernameExists) {
        this.$notify({
          type: 'error',
          title: '错误',
          message: '该用户名已被使用，请更换用户名'
        })
        return
      }
      
      // 验证密码格式（仅在创建新用户时）
      if (!this.isEditing && this.formData.password) {
        // 再次检查密码有效性
        this.checkPassword();
        
        if (!this.passwordValid) {
          this.$notify({
            type: 'error',
            title: '错误',
            message: '密码错误: ' + this.passwordError
          })
          return
        }
      }
      
      if (this.isEditing) {
        // 编辑现有用户
        const userData = { ...this.formData }
        // 如果密码为空，不更新密码
        if (!userData.password) {
          delete userData.password
        }
        
        userService.updateUser(userData.id, userData)
          .then(() => {
            this.closeModal()
            this.loadUsers() // 重新加载用户列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '用户信息已更新'
            })
          })
          .catch(error => {
            console.error('更新用户失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '更新用户失败: ' + (error.response?.data?.message || error.message)
            })
          })
      } else {
        // 创建新用户
        userService.createUser(this.formData)
          .then(() => {
            this.closeModal()
            this.loadUsers() // 重新加载用户列表
            this.$notify({
              type: 'success',
              title: '成功',
              message: '用户已创建'
            })
          })
          .catch(error => {
            console.error('创建用户失败:', error)
            this.$notify({
              type: 'error',
              title: '错误',
              message: '创建用户失败: ' + (error.response?.data?.message || error.message)
            })
          })
      }
    },
    
    confirmDelete(user) {
      this.userToDelete = user
      this.showDeleteModal = true
    },
    
    cancelDelete() {
      this.userToDelete = null
      this.showDeleteModal = false
    },
    
    deleteUser() {
      if (!this.userToDelete) return
      
      userService.deleteUser(this.userToDelete.id)
        .then(() => {
          this.users = this.users.filter(user => user.id !== this.userToDelete.id)
          this.filterUsers() // 更新筛选后的列表
          this.cancelDelete()
          this.$notify({
            type: 'success',
            title: '成功',
            message: '用户已删除'
          })
        })
        .catch(error => {
          console.error('删除用户失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '删除用户失败: ' + (error.response?.data?.message || error.message)
          })
        })
    },
    
    toggleUserStatus(user) {
      const newStatus = !user.active
      const updatedUser = { ...user, active: newStatus }
      
      userService.updateUser(user.id, updatedUser)
        .then(() => {
          // 更新本地用户状态
          const index = this.users.findIndex(u => u.id === user.id)
          if (index !== -1) {
            this.users[index].active = newStatus
            this.filterUsers() // 更新筛选后的列表
          }
          
          this.$notify({
            type: 'success',
            title: '成功',
            message: `用户已${newStatus ? '启用' : '停用'}`
          })
        })
        .catch(error => {
          console.error('更新用户状态失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '更新用户状态失败: ' + (error.response?.data?.message || error.message)
          })
        })
    },
    
    getUserStatusText(user) {
      if (!user.active) return '停用'
      if (user.canComment === false) return '禁言'
      return '正常'
    },
    
    getUserStatusClass(user) {
      if (!user.active) return 'status-inactive'
      if (user.canComment === false) return 'status-banned'
      return 'status-active'
    },
    
    toggleCommentStatus(user) {
      const canComment = user.canComment !== false ? false : true
      const updatedUser = { ...user, canComment }
      
      userService.updateUser(user.id, updatedUser)
        .then(() => {
          // 更新本地用户状态
          const index = this.users.findIndex(u => u.id === user.id)
          if (index !== -1) {
            this.users[index].canComment = canComment
            this.filterUsers() // 更新筛选后的列表
          }
          
          this.$notify({
            type: 'success',
            title: '成功',
            message: `用户已${canComment ? '解除禁言' : '禁言'}`
          })
        })
        .catch(error => {
          console.error('更新用户评论权限失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '更新用户评论权限失败: ' + (error.response?.data?.message || error.message)
          })
        })
    },
    
    // 添加密码校验方法
    checkPassword() {
      const password = this.formData.password;
      
      if (!password || password.length === 0) {
        this.passwordValid = true;
        this.passwordError = '';
        return;
      }
      
      if (password.length < 8) {
        this.passwordValid = false;
        this.passwordError = '密码长度不足';
        return;
      }
      
      if (!/[a-zA-Z]/.test(password)) {
        this.passwordValid = false;
        this.passwordError = '密码不能为纯数字';
        return;
      }
      
      if (!/[0-9]/.test(password)) {
        this.passwordValid = false;
        this.passwordError = '密码不能为纯字母';
        return;
      }
      
      this.passwordValid = true;
      this.passwordError = '';
    },
    
    // 检查用户名是否已存在
    handleUsernameInput() {
      // 编辑模式下无需检查用户名
      if (this.isEditing) return;
      
      // 清除之前的timeout，防止多次快速输入触发多个请求
      if (this.usernameCheckTimeout) {
        clearTimeout(this.usernameCheckTimeout)
      }
      
      // 重置状态
      this.usernameChecking = true
      this.usernameExists = false
      this.usernameAvailable = false
      
      // 如果用户名为空，不检查
      if (!this.formData.username) {
        this.usernameChecking = false
        return
      }
      
      // 设置较短的延迟，提高实时响应性但避免每次按键都发请求
      this.usernameCheckTimeout = setTimeout(() => {
        authService.checkUsernameExists(this.formData.username)
          .then(response => {
            this.usernameChecking = false
            this.usernameExists = response.data.exists
            this.usernameAvailable = !response.data.exists
          })
          .catch(error => {
            this.usernameChecking = false
            console.error('检查用户名失败:', error)
          })
      }, 300)
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
}

.filters {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
  box-sizing: border-box;
}

.search-box {
  width: 75%; /* 直接设置宽度为75% */
}

.search-box input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.role-filter {
  width: 25%; /* 直接设置宽度为25% */
}

.role-filter select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
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

.role-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 0.85em;
  font-weight: 500;
}

.role-admin {
  background-color: #ffcdd2;
  color: #c62828;
}

.role-student {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 0.85em;
  font-weight: 500;
}

.status-active {
  background-color: #c8e6c9;
  color: #2e7d32;
}

.status-banned {
  background-color: #fff9c4;
  color: #f57f17;
}

.status-inactive {
  background-color: #f5f5f5;
  color: #757575;
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

.btn-toggle {
  background-color: #e8f5e9; /* 浅绿色背景 */
  color: #2e7d32; /* 深绿色文字 */
}

.btn-toggle:hover {
  background-color: #c8e6c9; /* 悬停时深一点的绿色 */
}

.btn-deactivate {
  background-color: #f3e5f5; /* 浅紫色背景 */
  color: #6a1b9a; /* 深紫色文字 */
}

.btn-deactivate:hover {
  background-color: #e1bee7; /* 悬停时深一点的紫色 */
}

.btn-activate {
  background-color: #e0f7fa; /* 浅青色背景 */
  color: #006064; /* 深青色文字 */
}

.btn-activate:hover {
  background-color: #b2ebf2; /* 悬停时深一点的青色 */
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

/* 模态框样式优化 */
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
  padding: 0 15px; /* 添加左右内边距，确保在小屏幕上有边距 */
  box-sizing: border-box; /* 确保padding不会增加总宽度 */
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 100%; /* 改为100%宽度 */
  max-width: 450px; /* 减小最大宽度，更适合编辑表单 */
  max-height: 90vh;
  overflow-y: auto;
  overflow-x: hidden; /* 防止水平滚动 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  margin: 0 auto; /* 居中 */
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky; /* 固定在顶部 */
  top: 0;
  background: white; /* 确保背景色 */
  z-index: 1; /* 确保在内容之上 */
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  white-space: nowrap; /* 防止标题换行 */
  overflow: hidden;
  text-overflow: ellipsis; /* 文本溢出时显示省略号 */
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
  width: 100%;
  box-sizing: border-box; /* 确保padding不会增加宽度 */
}

.form-group {
  margin-bottom: 15px;
  width: 100%; /* 确保表单元素占满宽度 */
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  word-break: break-word; /* 允许长标签文本自动换行 */
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="password"],
.form-group select {
  width: 100%;
  max-width: 100%; /* 添加最大宽度限制 */
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box; /* 确保padding不会增加宽度 */
  overflow: hidden; /* 防止内容溢出 */
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  flex-wrap: wrap; /* 在小屏幕上允许按钮换行 */
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

.btn-comment-toggle {
  padding: 5px 8px;
  border: none;
  border-radius: 3px;
  font-size: 0.85em;
  cursor: pointer;
}

.btn-ban-comment {
  background-color: #fff8e1;
  color: #ff8f00;
}

.btn-ban-comment:hover {
  background-color: #ffecb3;
}

.btn-allow-comment {
  background-color: #e0f7fa;
  color: #0097a7;
}

.btn-allow-comment:hover {
  background-color: #b2ebf2;
}

.checkbox-group {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

/* 改进的表单布局 */
.form-group-container {
  display: flex;
  width: 100%;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap; /* 添加flex-wrap以确保元素可以换行 */
}

.form-group-left, .form-group-right {
  flex: 1; /* 使用flex: 1代替固定宽度，更灵活适应不同宽度 */
  min-width: 150px; /* 设置最小宽度防止元素过窄 */
  margin-bottom: 0;
}

@media (max-width: 480px) {
  .form-group-container {
    flex-direction: column;
    gap: 15px;
  }
  
  .form-group-left, .form-group-right {
    width: 100%;
    min-width: unset; /* 在小屏幕上取消最小宽度限制 */
  }
  
  .form-actions {
    justify-content: space-between; /* 在小屏幕上按钮两端对齐 */
  }
}

/* 按钮样式 */
.btn-cancel, .btn-submit {
  min-width: 80px; /* 确保按钮有最小宽度 */
}

/* 确保下拉框不会超出边界 */
select {
  width: 100%;
  box-sizing: border-box;
  text-overflow: ellipsis; /* 文本过长时显示省略号 */
  appearance: none; /* 移除默认样式 */
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8' viewBox='0 0 8 8'%3E%3Cpath fill='%23666' d='M0 2l4 4 4-4z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 8px;
  padding-right: 30px; /* 为下拉图标留出空间 */
  max-width: 100%; /* 确保不会超出父容器 */
}

.required-field {
  color: #d93025;
  margin-left: 2px;
}

.form-text {
  font-size: 12px;
  margin-top: 5px;
  display: block;
}

.text-muted {
  color: #6c757d;
}

.text-danger {
  color: #d93025;
}

.text-success {
  color: #0d8050;
}

.password-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.password-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #555;
  padding: 0;
  display: flex;
  align-items: center;
  z-index: 10;
  transition: color 0.2s ease;
}

.password-toggle-btn:hover {
  color: #1976d2;
}

.password-toggle-btn svg {
  transition: all 0.2s ease;
}

.form-note {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}
</style> 