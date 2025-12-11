import { createStore } from 'vuex'
import auth from './modules/auth'
import courses from './modules/courses'
import teachers from './modules/teachers'
import faculties from './modules/faculties'
import reviews from './modules/reviews'
import recommendations from './modules/recommendations'

export default createStore({
  state: {
    loading: false,
    error: null
  },
  getters: {
    isLoading: state => state.loading,
    hasError: state => !!state.error,
    error: state => state.error
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    },
    CLEAR_ERROR(state) {
      state.error = null
    }
  },
  actions: {
    setLoading({ commit }, loading) {
      commit('SET_LOADING', loading)
    },
    setError({ commit }, error) {
      commit('SET_ERROR', error)
    },
    clearError({ commit }) {
      commit('CLEAR_ERROR')
    }
  },
  modules: {
    auth,
    courses,
    teachers,
    faculties,
    reviews,
    recommendations
  }
}) 