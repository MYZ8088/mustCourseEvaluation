import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('../views/Courses.vue'),
    meta: { publicPage: true }
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('../views/CourseDetail.vue'),
    meta: { publicPage: true }
  },
  {
    path: '/teachers',
    name: 'Teachers',
    component: () => import('../views/Teachers.vue'),
    meta: { publicPage: true }
  },
  {
    path: '/teachers/:id',
    name: 'TeacherDetail',
    component: () => import('../views/TeacherDetail.vue'),
    meta: { publicPage: true }
  },
  {
    path: '/recommendations',
    name: 'Recommendations',
    component: () => import('../views/Recommendations.vue'),
    meta: { publicPage: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Index.vue'),
    meta: { requiresAuth: true, adminOnly: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('../views/admin/Courses.vue')
      },
      {
        path: 'teachers',
        name: 'AdminTeachers',
        component: () => import('../views/admin/Teachers.vue')
      },
      {
        path: 'faculties',
        name: 'AdminFaculties',
        component: () => import('../views/admin/Faculties.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('../views/admin/Reviews.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 直接从localStorage获取登录状态
const isUserLoggedIn = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) return false

    const user = JSON.parse(userStr)
    // 确保token存在且不为空
    return !!user && !!user.token && user.token.length > 0
  } catch (e) {
    console.error('Error checking login status:', e)
    return false
  }
}

// 检查用户是否是管理员
const isUserAdmin = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) return false
    const user = JSON.parse(userStr)
    return !!user && user.role === 'ROLE_ADMIN'
  } catch (e) {
    console.error('Error checking admin status:', e)
    return false
  }
}

router.beforeEach((to, from, next) => {
  // 刷新Vuex中的认证状态
  store.dispatch('auth/refreshState')

  // 直接从localStorage读取登录状态，不依赖Vuex
  const isLoggedIn = isUserLoggedIn()
  const isAdmin = isUserAdmin()

  console.log('Route guard:', to.path, 'isLoggedIn:', isLoggedIn, 'isAdmin:', isAdmin)

  // 如果页面标记为公共页面，允许无认证访问
  if (to.matched.some(record => record.meta.publicPage)) {
    console.log('访问公共页面:', to.path)
    next()
    return
  }

  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else if (to.matched.some(record => record.meta.adminOnly) && !isAdmin) {
      next({ path: '/' })
    } else {
      next()
    }
  }
  // 检查是否是仅游客可访问的页面
  else if (to.matched.some(record => record.meta.guestOnly)) {
    if (isLoggedIn) {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router 