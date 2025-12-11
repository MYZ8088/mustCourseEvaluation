import CourseService from '@/services/course.service'

export default {
  namespaced: true,
  state: {
    courses: [],
    course: null,
    loading: false,
    error: null
  },
  getters: {
    allCourses: state => state.courses,
    currentCourse: state => state.course,
    isLoading: state => state.loading,
    error: state => state.error
  },
  mutations: {
    SET_COURSES(state, courses) {
      state.courses = courses
    },
    SET_COURSE(state, course) {
      state.course = course
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  actions: {
    fetchCourses({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return CourseService.getAllCourses()
        .then(response => {
          commit('SET_COURSES', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取课程列表失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    },
    fetchCourseById({ commit }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return CourseService.getCourseById(id)
        .then(response => {
          commit('SET_COURSE', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取课程详情失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    }
  }
} 