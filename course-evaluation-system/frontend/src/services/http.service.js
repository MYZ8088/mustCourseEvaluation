import axios from 'axios'
import store from '@/store'

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8088/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 60000  // AI调用可能需要较长时间，增加到60秒
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 如果设置了Skip-Auth头，则跳过认证
    if (config.headers && config.headers['Skip-Auth']) {
      delete config.headers['Skip-Auth']
      return config
    }
    
    // 从本地存储获取token
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const userData = JSON.parse(userStr)
        if (userData && userData.token) {
          // 添加授权头
          config.headers.Authorization = `Bearer ${userData.token}`
        }
      } catch (e) {
        console.error('解析用户数据出错:', e)
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 公开API路径列表（不需要登录即可访问）
const publicPaths = [
  'courses',
  'teachers',
  'faculties',
  'reviews/course',
  'reviews/teacher',
  'auth/'
]

// 检查是否是公开API
const isPublicApi = (url) => {
  return publicPaths.some(path => url.includes(path))
}

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response) {
      // 检查是否是登录请求
      const isLoginRequest = error.config.url.includes('auth/login');
      const requestUrl = error.config.url || '';
      
      // 定制化错误消息
      switch (error.response.status) {
        case 400:
          if (isLoginRequest) {
            // 登录请求的400状态码通常意味着用户名或密码错误
            error.message = '用户名或密码错误，请重新输入';
          } else {
            // 其他请求的400错误给出更友好的提示
            error.message = '提交的信息有误，请检查并重新填写';
          }
          break;
        case 401:
          error.message = '未登录或登录已过期，请重新登录';
          // 只有非公开API才触发登录跳转
          if (!isPublicApi(requestUrl)) {
            // 清除用户状态
            store.dispatch('auth/logout');
            // 如果不是登录页面，跳转到登录页
            if (window.location.pathname !== '/login') {
              window.location.href = '/login';
            }
          }
          break;
        case 403:
          if (isLoginRequest) {
            error.message = '用户名或密码错误，请重新输入';
          } else {
            error.message = '您没有权限执行此操作';
          }
          break;
        case 404:
          if (isLoginRequest) {
            error.message = '用户不存在，请先注册账号';
          } else {
            error.message = '请求的资源不存在';
          }
          break;
        case 500:
          error.message = '服务器内部错误，请稍后再试';
          break;
        default:
          error.message = '网络错误，请稍后重试';
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      error.message = '服务器无响应，请检查网络连接';
    } else {
      // 请求配置有误
      error.message = '请求异常，请稍后重试';
    }
    
    return Promise.reject(error);
  }
)

export default apiClient 