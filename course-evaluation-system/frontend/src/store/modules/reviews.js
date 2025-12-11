import ReviewService from '@/services/review.service'

export default {
  namespaced: true,
  state: {
    reviews: [],
    review: null,
    loading: false,
    error: null
  },
  getters: {
    allReviews: state => state.reviews,
    currentReview: state => state.review,
    isLoading: state => state.loading,
    error: state => state.error
  },
  mutations: {
    SET_REVIEWS(state, reviews) {
      state.reviews = reviews
    },
    SET_REVIEW(state, review) {
      state.review = review
    },
    ADD_REVIEW(state, review) {
      state.reviews.push(review)
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  actions: {
    fetchReviews({ commit }, { courseId, teacherId }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      let promise
      
      if (courseId) {
        promise = ReviewService.getCourseReviews(courseId)
      } else if (teacherId) {
        promise = ReviewService.getTeacherReviews(teacherId)
      } else {
        promise = ReviewService.getAllReviews()
      }
      
      return promise
        .then(response => {
          commit('SET_REVIEWS', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '获取评价失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    },
    submitReview({ commit }, review) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      return ReviewService.createReview(review)
        .then(response => {
          commit('ADD_REVIEW', response.data)
          return Promise.resolve(response.data)
        })
        .catch(error => {
          commit('SET_ERROR', error.response?.data?.message || '提交评价失败')
          return Promise.reject(error)
        })
        .finally(() => {
          commit('SET_LOADING', false)
        })
    }
  }
} 