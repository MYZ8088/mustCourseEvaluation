import http from './http.service'

class AuthService {
  login(username, password) {
    return http
      .post('auth/login', {
        username,
        password
      })
      .then(response => {
        if (response.data.token) {
          // 保存用户信息和登录时间
          const userData = {
            ...response.data,
            loginTime: new Date().getTime()
          }
          localStorage.setItem('user', JSON.stringify(userData))
          console.log('用户登录成功，保存token到localStorage', userData.username)
        }
        return response.data
      })
  }

  logout() {
    localStorage.removeItem('user')
    console.log('用户登出，已清除localStorage中的token')
  }

  register(user) {
    return http.post('auth/register', {
      username: user.username,
      email: user.email,
      password: user.password,
      emailCode: user.emailCode
    })
  }

  // 发送邮箱验证码（注册用）
  sendEmailVerification(email) {
    return http.post('auth/send-email-verification', { email })
  }

  // 邮箱验证码登录
  loginByEmail(email, emailCode) {
    return http
      .post('auth/login-by-email', {
        email,
        emailCode
      })
      .then(response => {
        if (response.data.token) {
          // 保存用户信息和登录时间
          const userData = {
            ...response.data,
            loginTime: new Date().getTime()
          }
          localStorage.setItem('user', JSON.stringify(userData))
          console.log('邮箱验证码登录成功，保存token到localStorage', userData.username)
        }
        return response.data
      })
  }

  // 发送登录验证码
  sendLoginVerification(email) {
    return http.post('auth/send-login-verification', { email })
  }

  // 检查用户是否已登录
  isLoggedIn() {
    try {
      const userStr = localStorage.getItem('user')
      if (!userStr) {
        console.log('localStorage中没有用户信息')
        return false
      }

      const user = JSON.parse(userStr)
      const isValid = !!user && !!user.token && user.token.length > 0
      console.log('检查登录状态:', isValid ? '已登录' : '未登录', user ? user.username : '')
      return isValid
    } catch (e) {
      console.error('检查登录状态出错:', e)
      return false
    }
  }

  // 获取当前用户
  getCurrentUser() {
    try {
      const userStr = localStorage.getItem('user')
      return userStr ? JSON.parse(userStr) : null
    } catch (e) {
      console.error('获取当前用户出错:', e)
      return null
    }
  }

  // 刷新token (如果实现了刷新token的API，可以在这里调用)
  refreshToken() {
    const user = this.getCurrentUser()
    if (user && user.token) {
      return http.post('auth/refresh-token', { token: user.token })
        .then(response => {
          if (response.data.token) {
            // 更新localStorage中的token
            const updatedUser = {
              ...user,
              token: response.data.token,
              loginTime: new Date().getTime()
            }
            localStorage.setItem('user', JSON.stringify(updatedUser))
            console.log('Token已刷新')
          }
          return response.data
        })
    }
    return Promise.reject('No valid token found')
  }

  // 检查用户名是否已存在
  checkUsernameExists(username) {
    return http.get(`auth/check-username?username=${encodeURIComponent(username)}`)
  }
}

export default new AuthService() 