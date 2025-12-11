import http from './http.service'

class TeacherService {
  getTeachers() {
    return http.get('teachers')
      .catch(error => {
        // 如果是401错误(未授权)，可能是token问题，尝试直接请求
        if (error.response && error.response.status === 401) {
          console.log('教师列表获取失败，尝试作为公共API重试')
          // 使用不带token的请求重试
          return http.get('teachers', { 
            headers: { 'Skip-Auth': true } // 自定义标记，在拦截器中可以处理
          })
        }
        return Promise.reject(error)
      })
  }
  
  getTeacher(id) {
    return http.get(`teachers/${id}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('教师详情获取失败，尝试作为公共API重试')
          return http.get(`teachers/${id}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  getTeachersByFaculty(facultyId) {
    return http.get(`teachers/faculty/${facultyId}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('教师(按院系)获取失败，尝试作为公共API重试')
          return http.get(`teachers/faculty/${facultyId}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  createTeacher(data) {
    return http.post('teachers', data)
  }
  
  updateTeacher(id, data) {
    return http.put(`teachers/${id}`, data)
  }
  
  deleteTeacher(id) {
    return http.delete(`teachers/${id}`)
  }
}

export default new TeacherService() 