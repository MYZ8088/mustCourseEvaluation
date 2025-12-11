<template>
  <div class="register-container">
    <div class="register-form">
      <h1>课程评价系统 - 注册</h1>
      <div v-if="error" class="alert alert-danger">
        {{ error }}
      </div>
      <div v-if="message" class="alert alert-success">
        {{ message }}
      </div>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="user.username"
            @input="handleUsernameInput"
            required
            maxlength="9"
            class="form-control"
            placeholder="请输入用户名"
          />
          <small v-if="usernameChecking" class="form-text text-muted">正在检查用户名是否可用...</small>
          <small v-if="usernameExists" class="form-text text-danger">该用户名已被使用</small>
          <small v-if="usernameAvailable" class="form-text text-success">该用户名可用</small>
          <small class="form-text text-muted">用户名长度限制：3-9个字符</small>
        </div>
        <div class="form-group">
          <label for="email">电子邮箱</label>
          <input
            type="email"
            id="email"
            v-model="user.email"
            @input="validateEmail"
            required
            class="form-control"
            :class="{ 'is-invalid': user.email && !isValidEmail }"
            placeholder="请输入学校邮箱 (xxx@student.must.edu.mo)"
          />
          <small v-if="user.email && !isValidEmail" class="form-text text-danger">
            <i class="fas fa-exclamation-circle"></i> 请使用澳门科技大学学生邮箱 (@student.must.edu.mo)
          </small>
          <small v-else-if="user.email && isValidEmail" class="form-text text-success">
            <i class="fas fa-check-circle"></i> 邮箱格式正确
          </small>
          <small v-else class="form-text text-muted">
            仅支持澳门科技大学学生邮箱注册
          </small>
        </div>
        <div class="form-group">
          <label for="emailCode">邮箱验证码</label>
          <div class="verification-code-container">
            <input
              type="text"
              id="emailCode"
              v-model="user.emailCode"
              required
              class="form-control verification-input"
              placeholder="请输入验证码"
            />
            <button 
              type="button" 
              class="verification-button"
              @click="sendVerificationCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
            </button>
          </div>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="user.password"
              @input="checkPasswordStrength"
              required
              maxlength="32"
              class="form-control"
              placeholder="请输入密码"
              autocomplete="new-password"
            />
            <button
              v-if="user.password"
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
          <div v-if="user.password" class="password-strength-container">
            <div v-if="user.password.length < 8" class="password-strength-text text-danger">密码长度不足</div>
            <div v-else-if="!/[a-zA-Z]/.test(user.password)" class="password-strength-text text-danger">密码不能为纯数字</div>
            <div v-else-if="!/[0-9]/.test(user.password)" class="password-strength-text text-danger">密码不能为纯字母</div>
            <div v-else class="password-strength-text">密码强度: {{ passwordStrengthText }}</div>
            
            <div v-if="user.password.length >= 8 && /[a-zA-Z]/.test(user.password) && /[0-9]/.test(user.password)" class="password-strength-meter">
              <div 
                class="password-strength-value" 
                :style="{ width: passwordStrengthWidth, backgroundColor: passwordStrengthColor }"
              ></div>
            </div>
          </div>
          <small class="form-text text-muted">
            密码要求：长度8-32位，必须包含字母和数字，可包含特殊符号
          </small>
        </div>
        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <div class="password-input-container">
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              id="confirmPassword"
              v-model="user.confirmPassword"
              @input="checkPasswordMatch"
              required
              maxlength="32"
              class="form-control"
              placeholder="请再次输入密码"
              autocomplete="new-password"
            />
            <button
              v-if="user.confirmPassword"
              type="button"
              class="password-toggle-btn"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
                <path v-if="showConfirmPassword" d="M2 2l20 20" />
              </svg>
            </button>
          </div>
          <small v-if="user.confirmPassword" class="form-text" :class="passwordsMatch ? 'text-success' : 'text-danger'">
            {{ passwordsMatch ? '密码匹配' : '密码不匹配，请重新输入确认密码' }}
          </small>
        </div>
        <div class="form-group">
          <button class="btn btn-primary btn-block" :disabled="loading || !isFormValid">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>注册</span>
          </button>
        </div>
        <div class="form-group">
          <div class="login-link">
            已有账号？
            <router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      user: {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        emailCode: ''
      },
      loading: false,
      message: null,
      error: null,
      countdown: 0,
      timer: null,
      usernameChecking: false,
      usernameExists: false,
      usernameAvailable: false,
      passwordStrength: 0,
      usernameCheckTimeout: null,
      showPassword: false,
      showConfirmPassword: false,
      passwordsMatch: false,
      isValidEmail: false
    }
  },
  computed: {
    passwordStrengthText() {
      if (this.passwordStrength < 1) return ''
      if (this.passwordStrength === 1) return '弱'
      if (this.passwordStrength === 2) return '中'
      if (this.passwordStrength === 3) return '强'
      return '很强'
    },
    passwordStrengthColor() {
      if (this.passwordStrength === 1) return '#ff4d4f'
      if (this.passwordStrength === 2) return '#faad14'
      if (this.passwordStrength === 3) return '#52c41a'
      return '#008000' // 深绿色
    },
    passwordStrengthWidth() {
      return this.passwordStrength * 25 + '%'
    },
    isFormValid() {
      // 检查表单是否有效：用户名不存在、密码符合要求、两次密码一致等
      const usernameValid = this.user.username && 
                          this.user.username.length >= 3 && 
                          this.user.username.length <= 9
      
      const passwordValid = this.user.password && 
                          this.user.password.length >= 8 && 
                          this.user.password.length <= 32 && 
                          /[a-zA-Z]/.test(this.user.password) && 
                          /[0-9]/.test(this.user.password)
      
      return !this.usernameExists && 
             usernameValid && 
             this.user.email && 
             this.isValidEmail &&  // 新增：邮箱必须是学校邮箱
             passwordValid && 
             this.user.password === this.user.confirmPassword &&
             this.user.emailCode
    }
  },
  methods: {
    handleUsernameInput() {
      // 清除之前的timeout，防止多次快速输入触发多个请求
      if (this.usernameCheckTimeout) {
        clearTimeout(this.usernameCheckTimeout)
      }
      
      // 重置状态
      this.usernameChecking = true
      this.usernameExists = false
      this.usernameAvailable = false
      
      // 如果用户名为空，不检查
      if (!this.user.username) {
        this.usernameChecking = false
        return
      }
      
      // 设置较短的延迟，提高实时响应性但避免每次按键都发请求
      this.usernameCheckTimeout = setTimeout(() => {
        this.$store.dispatch('auth/checkUsernameExists', this.user.username)
          .then(response => {
            this.usernameChecking = false
            this.usernameExists = response.exists
            this.usernameAvailable = !response.exists
          })
          .catch(error => {
            this.usernameChecking = false
            console.error('检查用户名失败:', error)
          })
      }, 300) // 减少延迟时间，提高实时性
    },
    
    checkPasswordStrength() {
      if (!this.user.password) {
        this.passwordStrength = 0
        return
      }
      
      // 初始强度为0
      let level = 0
      
      // 条件1：密码长度 >= 8位
      const hasMinLength = this.user.password.length >= 8
      
      // 条件2：包含字母
      const hasLetter = /[a-zA-Z]/.test(this.user.password)
      
      // 条件3：包含数字
      const hasDigit = /[0-9]/.test(this.user.password)
      
      // 条件4：包含特殊字符
      const hasSpecial = /[^a-zA-Z0-9]/.test(this.user.password)
      
      // 条件5：同时包含大小写字母
      const hasUpperCase = /[A-Z]/.test(this.user.password)
      const hasLowerCase = /[a-z]/.test(this.user.password)
      const hasMixedCase = hasUpperCase && hasLowerCase
      
      // 评分规则调整：
      // 首先要求长度>=8且包含字母和数字（这是基本要求）
      if (hasMinLength && hasLetter && hasDigit) {
        // 基本要求满足，起始为1级
        level = 1 // 弱
        
        // 额外条件计数
        let additionalConditions = 0
        if (hasSpecial) additionalConditions++
        if (hasMixedCase) additionalConditions++
        if (this.user.password.length >= 12) additionalConditions++
        
        // 根据满足的额外条件提高强度
        if (additionalConditions >= 1) level = 2 // 中
        if (additionalConditions >= 2) level = 3 // 强
        if (additionalConditions >= 3) level = 4 // 很强
      }
      
      this.passwordStrength = level
    },
    
    handleRegister() {
      this.message = null
      this.error = null
      
      // 验证用户名长度
      if (this.user.username.length < 3) {
        this.error = '用户名长度不能少于3个字符'
        return
      }
      
      // 验证密码格式
      if (this.user.password.length < 8 || 
          this.user.password.length > 32 ||
          !/[a-zA-Z]/.test(this.user.password) || 
          !/[0-9]/.test(this.user.password)) {
        this.error = '密码长度必须在8-32位之间，并且必须包含字母和数字'
        return
      }
      
      if (this.user.password !== this.user.confirmPassword) {
        this.error = '两次输入的密码不一致'
        return
      }
      
      if (this.usernameExists) {
        this.error = '该用户名已被使用，请更换用户名'
        return
      }
      
      this.loading = true
      
      this.$store.dispatch('auth/register', {
        username: this.user.username,
        email: this.user.email,
        password: this.user.password,
        emailCode: this.user.emailCode
      })
        .then(() => {
          this.loading = false
          this.message = '注册成功！请登录您的账号。'
          // 清空表单
          this.user = {
            username: '',
            email: '',
            password: '',
            confirmPassword: '',
            emailCode: ''
          }
          
          // 3秒后跳转到登录页面
          setTimeout(() => {
            this.$router.push('/login')
          }, 3000)
        })
        .catch(error => {
          this.loading = false
          this.error = error.response?.data?.message || '注册失败，请稍后再试'
        })
    },
    sendVerificationCode() {
      if (this.user.email === '') {
        this.error = '请先输入电子邮箱'
        return
      }
      
      // 验证邮箱格式，必须是学校邮箱
      const emailRegex = /^[^\s@]+@student\.must\.edu\.mo$/i
      if (!emailRegex.test(this.user.email)) {
        this.error = '请使用澳门科技大学学生邮箱 (@student.must.edu.mo)'
        return
      }
      
      // 开始倒计时
      this.countdown = 60
      this.timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(this.timer)
        }
      }, 1000)
      
      // 发送验证码请求
      this.$store.dispatch('auth/sendEmailVerification', {
        email: this.user.email
      })
        .then(() => {
          this.message = '验证码已发送到您的邮箱，请查收'
        })
        .catch(error => {
          this.error = error.response?.data?.message || '验证码发送失败，请稍后再试'
          this.countdown = 0
          clearInterval(this.timer)
        })
    },
    checkPasswordMatch() {
      if (!this.user.confirmPassword) {
        this.passwordsMatch = false;
        return;
      }
      this.passwordsMatch = this.user.password === this.user.confirmPassword;
    },
    
    validateEmail() {
      if (!this.user.email) {
        this.isValidEmail = false;
        return;
      }
      // 验证邮箱格式并且必须是 @student.must.edu.mo 后缀
      const emailRegex = /^[^\s@]+@student\.must\.edu\.mo$/i;
      this.isValidEmail = emailRegex.test(this.user.email);
    }
  },
  beforeUnmount() {
    // 清除定时器
    if (this.timer) {
      clearInterval(this.timer)
    }
    if (this.usernameCheckTimeout) {
      clearTimeout(this.usernameCheckTimeout)
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 20px 0;
}

.register-form {
  width: 100%;
  max-width: 500px;
  padding: 30px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #4a6bff;
}

.form-control.is-invalid {
  border-color: #d93025;
  background-color: #fff8f8;
}

.form-control.is-invalid:focus {
  border-color: #d93025;
  box-shadow: 0 0 0 2px rgba(217, 48, 37, 0.1);
}

.verification-code-container {
  display: flex;
  gap: 10px;
}

.verification-input {
  flex: 1;
}

.verification-button {
  padding: 12px 15px;
  background-color: #4a6bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.verification-button:hover {
  background-color: #3955d9;
}

.verification-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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
  color: #4a6bff;
}

.password-toggle-btn svg {
  transition: all 0.2s ease;
}

.password-strength-container {
  margin-top: 8px;
}

.password-strength-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.password-strength-meter {
  height: 6px;
  background-color: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.password-strength-value {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s, background-color 0.3s;
}

.form-text {
  font-size: 14px;
  display: block;
  margin-top: 5px;
}

.text-danger {
  color: #d93025;
}

.text-success {
  color: #0d8050;
}

.text-muted {
  color: #6c757d;
}

.btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  box-sizing: border-box;
}

.btn-primary {
  background-color: #4a6bff;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #3955d9;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.alert {
  padding: 12px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.alert-danger {
  background-color: #ffeded;
  color: #d93025;
  border: 1px solid #f8d7da;
}

.alert-success {
  background-color: #edfff4;
  color: #0d8050;
  border: 1px solid #d4edda;
}

.login-link {
  text-align: center;
  margin-top: 15px;
}

.login-link a {
  color: #4a6bff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style> 