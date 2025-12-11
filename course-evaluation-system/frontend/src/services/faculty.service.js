import http from './http.service'

class FacultyService {
  constructor() {
    // 缓存院系数据（院系数据很少变化，可以缓存）
    this._facultiesCache = null
    this._cacheExpiry = null
    this._cacheDuration = 5 * 60 * 1000 // 5分钟缓存
  }

  getFaculties() {
    // 检查缓存是否有效
    const now = Date.now()
    if (this._facultiesCache && this._cacheExpiry && now < this._cacheExpiry) {
      return Promise.resolve({ data: this._facultiesCache })
    }
    
    // 发起请求并缓存结果
    return http.get('faculties').then(response => {
      this._facultiesCache = response.data
      this._cacheExpiry = now + this._cacheDuration
      return response
    })
  }
  
  getFaculty(id) {
    return http.get(`faculties/${id}`)
  }
  
  createFaculty(data) {
    // 清除缓存
    this._facultiesCache = null
    this._cacheExpiry = null
    return http.post('faculties', data)
  }
  
  updateFaculty(id, data) {
    // 清除缓存
    this._facultiesCache = null
    this._cacheExpiry = null
    return http.put(`faculties/${id}`, data)
  }
  
  deleteFaculty(id) {
    // 清除缓存
    this._facultiesCache = null
    this._cacheExpiry = null
    return http.delete(`faculties/${id}`)
  }
  
  // 手动清除缓存
  clearCache() {
    this._facultiesCache = null
    this._cacheExpiry = null
  }
}

export default new FacultyService() 