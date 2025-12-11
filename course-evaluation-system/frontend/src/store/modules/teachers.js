import TeacherService from '@/services/teacher.service'

export default {
  namespaced: true,
  state: {
    teachers: [],
    teacher: null,
    loading: false,
    error: null
  },
  getters: {
    allTeachers: state => state.teachers,
    currentTeacher: state => state.teacher,
    isLoading: state => state.loading,
    error: state => state.error
  },
  mutations: {
    SET_TEACHERS(state, teachers) {
      state.teachers = teachers
    },
    SET_TEACHER(state, teacher) {
      state.teacher = teacher
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  actions: {
    fetchTeachers({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return TeacherService.getTeachers()
        .then(response => {
          commit('SET_TEACHERS', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取教师列表失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    },
    fetchTeacherById({ commit }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return TeacherService.getTeacher(id)
        .then(response => {
          commit('SET_TEACHER', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取教师详情失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    }
  }
} 