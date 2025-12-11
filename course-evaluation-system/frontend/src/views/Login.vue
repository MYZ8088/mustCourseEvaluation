<template>
  <div class="login-container">
    <div class="login-form">
      <h1>课程评价系统 - 登录</h1>
      <div v-if="error" class="alert alert-danger">
        {{ error }}
      </div>
      <div v-if="message" class="alert alert-success">
        {{ message }}
      </div>
      
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: loginType === 'password' }"
          @click="switchLoginType('password')"
        >
          账号密码登录
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: loginType === 'email' }"
          @click="switchLoginType('email')"
        >
          邮箱验证码登录
        </button>
      </div>
      
      <!-- 账号密码登录表单 -->
      <form v-if="loginType === 'password'" @submit.prevent="handlePasswordLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="passwordForm.username"
            required
            class="form-control"
            placeholder="请输入用户名"
          />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="passwordForm.password"
              required
              class="form-control"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
            <button
              v-if="passwordForm.password"
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
        </div>
        <div class="form-group">
          <button class="btn btn-primary btn-block" :disabled="loading || !isPasswordFormValid">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>登录</span>
          </button>
        </div>
      </form>
      
      <!-- 邮箱验证码登录表单 -->
      <form v-if="loginType === 'email'" @submit.prevent="handleEmailLogin">
        <div class="form-group">
          <label for="email">电子邮箱</label>
          <input
            type="email"
            id="email"
            v-model="emailForm.email"
            @input="validateEmail"
            required
            class="form-control"
            :class="{ 'is-invalid': emailForm.email && !isValidEmail }"
            placeholder="请输入注册邮箱"
          />
          <small v-if="emailForm.email && !isValidEmail" class="form-text text-danger">
            <i class="fas fa-exclamation-circle"></i> 请输入有效的邮箱地址
          </small>
          <small v-else-if="emailForm.email && isValidEmail" class="form-text text-success">
            <i class="fas fa-check-circle"></i> 邮箱格式正确
          </small>
        </div>
        <div class="form-group">
          <label for="emailCode">邮箱验证码</label>
          <div class="verification-code-container">
            <input
              type="text"
              id="emailCode"
              v-model="emailForm.emailCode"
              required
              class="form-control verification-input"
              placeholder="请输入验证码"
            />
            <button 
              type="button" 
              class="verification-button"
              @click="sendVerificationCode"
              :disabled="countdown > 0 || !isValidEmail"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
            </button>
          </div>
        </div>
        <div class="form-group">
          <button class="btn btn-primary btn-block" :disabled="loading || !isEmailFormValid">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>登录</span>
          </button>
        </div>
      </form>
      
      <div class="form-group">
        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginType: 'password',
      passwordForm: {
        username: '',
        password: ''
      },
      emailForm: {
        email: '',
        emailCode: ''
      },
      loading: false,
      message: null,
      error: null,
      countdown: 0,
      timer: null,
      showPassword: false,
      isValidEmail: false
    }
  },
  computed: {
    isPasswordFormValid() {
      return this.passwordForm.username && 
             this.passwordForm.username.length >= 3 && 
             this.passwordForm.username.length <= 9 &&
             this.passwordForm.password && 
             this.passwordForm.password.length >= 8
    },
    isEmailFormValid() {
      return this.isValidEmail && 
             this.emailForm.emailCode && 
             this.emailForm.emailCode.length > 0
    }
  },
  methods: {
    switchLoginType(type) {
      this.loginType = type
      this.error = null
      this.message = null
    },
    
    validateEmail() {
      if (!this.emailForm.email) {
        this.isValidEmail = false
        return
      }
      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      this.isValidEmail = emailRegex.test(this.emailForm.email)
    },
    
    handlePasswordLogin() {
      this.message = null
      this.error = null
      this.loading = true
      
      this.$store.dispatch('auth/login', {
        username: this.passwordForm.username,
        password: this.passwordForm.password
      })
        .then(() => {
          this.loading = false
          this.message = '登录成功！'
          
          // 跳转到之前尝试访问的页面或首页
          const redirect = this.$route.query.redirect || '/'
          setTimeout(() => {
            this.$router.push(redirect)
          }, 500)
        })
        .catch(error => {
          this.loading = false
          this.error = error.response?.data?.message || '登录失败，请检查用户名和密码'
        })
    },
    
    handleEmailLogin() {
      this.message = null
      this.error = null
      this.loading = true
      
      this.$store.dispatch('auth/loginByEmail', {
        email: this.emailForm.email,
        emailCode: this.emailForm.emailCode
      })
        .then(() => {
          this.loading = false
          this.message = '登录成功！'
          
          // 跳转到之前尝试访问的页面或首页
          const redirect = this.$route.query.redirect || '/'
          setTimeout(() => {
            this.$router.push(redirect)
          }, 500)
        })
        .catch(error => {
          this.loading = false
          this.error = error.response?.data?.message || '登录失败，请检查验证码是否正确'
        })
    },
    
    sendVerificationCode() {
      if (!this.emailForm.email) {
        this.error = '请先输入电子邮箱'
        return
      }
      
      if (!this.isValidEmail) {
        this.error = '请输入有效的邮箱地址'
        return
      }
      
      this.error = null
      
      // 开始倒计时
      this.countdown = 60
      this.timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(this.timer)
        }
      }, 1000)
      
      // 发送登录验证码请求
      this.$store.dispatch('auth/sendLoginVerification', {
        email: this.emailForm.email
      })
        .then(() => {
          this.message = '验证码已发送到您的邮箱，请查收'
        })
        .catch(error => {
          this.error = error.response?.data?.message || '验证码发送失败，请稍后再试'
          this.countdown = 0
          clearInterval(this.timer)
        })
    }
  },
  beforeUnmount() {
    // 清除定时器
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 20px 0;
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 30px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-tabs {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 1px solid #e0e0e0;
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  transition: all 0.3s ease;
  position: relative;
}

.tab-btn:hover {
  color: #4a6bff;
}

.tab-btn.active {
  color: #4a6bff;
  font-weight: 500;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #4a6bff;
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
  transition: background-color 0.2s ease;
}

.verification-button:hover:not(:disabled) {
  background-color: #3955d9;
}

.verification-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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
  transition: background-color 0.2s ease;
}

.btn-primary:hover:not(:disabled) {
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

.register-link {
  text-align: center;
  margin-top: 15px;
}

.register-link a {
  color: #4a6bff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

.spinner-border {
  display: inline-block;
  width: 1rem;
  height: 1rem;
  vertical-align: text-bottom;
  border: 0.15em solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spinner-border 0.75s linear infinite;
  margin-right: 8px;
}

@keyframes spinner-border {
  to {
    transform: rotate(360deg);
  }
}
</style>









