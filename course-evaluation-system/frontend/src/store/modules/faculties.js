import FacultyService from '@/services/faculty.service'

export default {
  namespaced: true,
  state: {
    faculties: [],
    faculty: null,
    loading: false,
    error: null
  },
  getters: {
    allFaculties: state => state.faculties,
    currentFaculty: state => state.faculty,
    isLoading: state => state.loading,
    error: state => state.error
  },
  mutations: {
    SET_FACULTIES(state, faculties) {
      state.faculties = faculties
    },
    SET_FACULTY(state, faculty) {
      state.faculty = faculty
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  actions: {
    fetchFaculties({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return FacultyService.getFaculties()
        .then(response => {
          commit('SET_FACULTIES', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取学院列表失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    },
    fetchFacultyById({ commit }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return FacultyService.getFaculty(id)
        .then(response => {
          commit('SET_FACULTY', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取学院详情失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    }
  }
} 