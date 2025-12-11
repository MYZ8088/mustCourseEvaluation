import http from './http.service'

class UserService {
  getCurrentUser() {
    return http.get('users/me')
      .catch(error => {
        // 如果返回404或401尝试其他可能的路径
        if (error.response && (error.response.status === 404 || error.response.status === 401)) {
          console.log('用户信息获取失败，尝试备用API')
          return http.get('user/me')  // 尝试备用路径
        }
        return Promise.reject(error)
      })
  }
  
  getUsers() {
    console.log('正在获取用户列表...')
    return http.get('users')
  }
  
  getUser(id) {
    console.log(`正在获取用户ID:${id}的信息...`)
    return http.get(`users/${id}`)
  }
  
  createUser(data) {
    console.log('正在创建新用户...', data)
    // 使用auth/register接口创建用户，添加固定的emailCode
    // 注意：这是临时解决方案，管理员创建用户不应该需要邮箱验证码
    return http.post('auth/register', {
      ...data,
      emailCode: "ADMIN_CREATE" // 特殊标识，表示由管理员创建
    })
  }
  
  updateUser(id, data) {
    console.log(`正在更新用户ID:${id}的信息...`, data)
    return http.put(`users/${id}`, data)
      .then(response => {
        console.log(`用户ID:${id}更新成功`, response.data)
        return response
      })
      .catch(error => {
        console.error(`用户ID:${id}更新失败`, error)
        return Promise.reject(error)
      })
  }
  
  deleteUser(id) {
    console.log(`正在删除用户ID:${id}...`)
    return http.delete(`users/${id}`)
  }
}

export default new UserService() 