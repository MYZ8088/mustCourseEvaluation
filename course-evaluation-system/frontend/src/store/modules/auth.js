import AuthService from '@/services/auth.service'

// 简化从localStorage获取用户信息的函数
const getUserFromStorage = () => {
  try {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  } catch (e) {
    console.error('Error parsing user from localStorage:', e)
    return null
  }
}

// 初始化状态
const user = getUserFromStorage()
const initialState = user
  ? { status: { loggedIn: true }, user }
  : { status: { loggedIn: false }, user: null }

export default {
  namespaced: true,
  state: initialState,
  getters: {
    isLoggedIn: state => state.status.loggedIn,
    currentUser: state => state.user,
    isAdmin: state => state.status.loggedIn && state.user && state.user.role === 'ROLE_ADMIN',
    isModerator: state => state.status.loggedIn && state.user && state.user.role === 'ROLE_MODERATOR',
    authHeader: state => {
      if (state.status.loggedIn && state.user && state.user.token) {
        return { Authorization: 'Bearer ' + state.user.token }
      } else {
        return {}
      }
    }
  },
  mutations: {
    LOGIN_SUCCESS(state, user) {
      state.status.loggedIn = true
      state.user = user
    },
    LOGIN_FAILURE(state) {
      state.status.loggedIn = false
      state.user = null
    },
    LOGOUT(state) {
      state.status.loggedIn = false
      state.user = null
    },
    REGISTER_SUCCESS(state) {
      state.status.loggedIn = false
    },
    REGISTER_FAILURE(state) {
      state.status.loggedIn = false
    },
    UPDATE_USER(state, user) {
      state.user = { ...state.user, ...user }
    },
    // 刷新状态的mutation
    REFRESH_STATE(state) {
      const user = getUserFromStorage()
      if (user) {
        state.status.loggedIn = true
        state.user = user
      } else {
        state.status.loggedIn = false
        state.user = null
      }
    }
  },
  actions: {
    login({ commit }, { username, password }) {
      return AuthService.login(username, password).then(
        user => {
          commit('LOGIN_SUCCESS', user)
          return Promise.resolve(user)
        },
        error => {
          commit('LOGIN_FAILURE')
          return Promise.reject(error)
        }
      )
    },
    loginByEmail({ commit }, { email, emailCode }) {
      return AuthService.loginByEmail(email, emailCode).then(
        user => {
          commit('LOGIN_SUCCESS', user)
          return Promise.resolve(user)
        },
        error => {
          commit('LOGIN_FAILURE')
          return Promise.reject(error)
        }
      )
    },
    logout({ commit }) {
      AuthService.logout()
      commit('LOGOUT')
    },
    register({ commit }, user) {
      return AuthService.register(user).then(
        response => {
          commit('REGISTER_SUCCESS')
          return Promise.resolve(response.data)
        },
        error => {
          commit('REGISTER_FAILURE')
          return Promise.reject(error)
        }
      )
    },
    updateUser({ commit }, user) {
      commit('UPDATE_USER', user)
    },
    // 发送邮箱验证码（注册用）
    sendEmailVerification({ commit }, { email }) {
      return AuthService.sendEmailVerification(email).then(
        response => {
          return Promise.resolve(response.data)
        },
        error => {
          return Promise.reject(error)
        }
      )
    },
    // 发送登录验证码
    sendLoginVerification({ commit }, { email }) {
      return AuthService.sendLoginVerification(email).then(
        response => {
          return Promise.resolve(response.data)
        },
        error => {
          return Promise.reject(error)
        }
      )
    },
    // 检查用户名是否已存在
    checkUsernameExists({ commit }, username) {
      return AuthService.checkUsernameExists(username)
        .then(
          response => {
            return Promise.resolve(response.data)
          },
          error => {
            return Promise.reject(error)
          }
        )
    },
    // 刷新状态的action
    refreshState({ commit }) {
      commit('REFRESH_STATE')
      // 检查并在控制台输出登录状态，方便调试
      const user = getUserFromStorage()
      console.log('Auth state refreshed, loggedIn:', !!user, user ? 'User:' + user.username : '')
    }
  }
} 