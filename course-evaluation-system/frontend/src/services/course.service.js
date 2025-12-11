import http from './http.service'

class CourseService {
  getAllCourses() {
    return http.get('courses')
      .catch(error => {
        // 如果是401错误(未授权)，可能是token问题，尝试直接请求
        if (error.response && error.response.status === 401) {
          console.log('课程列表获取失败，尝试作为公共API重试')
          // 使用不带token的请求重试
          return http.get('courses', { 
            headers: { 'Skip-Auth': true } // 自定义标记，在拦截器中可以处理
          })
        }
        return Promise.reject(error)
      })
  }
  
  getCourseById(id) {
    return http.get(`courses/${id}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程详情获取失败，尝试作为公共API重试')
          return http.get(`courses/${id}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  getCoursesByFaculty(facultyId) {
    return http.get(`courses/faculty/${facultyId}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程(按院系)获取失败，尝试作为公共API重试')
          return http.get(`courses/faculty/${facultyId}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  getCoursesByTeacher(teacherId) {
    return http.get(`courses/teacher/${teacherId}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程(按教师)获取失败，尝试作为公共API重试')
          return http.get(`courses/teacher/${teacherId}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  getCoursesByType(type) {
    return http.get(`courses/type/${type}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程(按类型)获取失败，尝试作为公共API重试')
          return http.get(`courses/type/${type}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  searchCourses(keyword) {
    return http.get(`courses/search?keyword=${keyword}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程搜索失败，尝试作为公共API重试')
          return http.get(`courses/search?keyword=${keyword}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  createCourse(data) {
    return http.post('courses', data)
  }
  
  updateCourse(id, data) {
    return http.put(`courses/${id}`, data)
  }
  
  deleteCourse(id) {
    return http.delete(`courses/${id}`)
  }
  
  // 获取课程评价统计
  getCourseRatings(id) {
    return http.get(`courses/${id}/ratings`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          return http.get(`courses/${id}/ratings`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
}

export default new CourseService() 