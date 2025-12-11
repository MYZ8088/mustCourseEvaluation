<template>
  <nav class="navbar">
    <div class="navbar-container">
      <div class="navbar-logo">
        <router-link to="/">课程评价系统</router-link>
      </div>
      <div class="navbar-links">
        <router-link to="/courses">课程列表</router-link>
        <router-link to="/teachers">教师列表</router-link>
        <router-link to="/recommendations">AI推荐</router-link>
        <template v-if="isLoggedIn">
          <router-link to="/profile" class="user-profile">
            <i class="fas fa-user-circle"></i> {{ currentUser && currentUser.username || '个人中心' }}
          </router-link>
          <a href="#" @click.prevent="logout">退出登录</a>
          <span v-if="isAdmin">
            <router-link to="/admin">管理控制台</router-link>
          </span>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Navbar',
  computed: {
    ...mapGetters({
      isLoggedIn: 'auth/isLoggedIn',
      currentUser: 'auth/currentUser',
      isAdmin: 'auth/isAdmin'
    })
  },
  methods: {
    logout() {
      this.$store.dispatch('auth/logout')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.navbar {
  background-color: #2c3e50;
  color: white;
  padding: 15px 0;
}

.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.navbar-logo a {
  color: white;
  font-size: 20px;
  font-weight: bold;
  text-decoration: none;
}

.navbar-links {
  display: flex;
  gap: 20px;
}

.navbar-links a {
  color: #ddd;
  text-decoration: none;
  transition: color 0.3s;
}

.navbar-links a:hover,
.navbar-links a.router-link-active {
  color: white;
}

.user-profile {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #ddd !important;
}

.user-profile i {
  margin-right: 5px;
}
</style> 