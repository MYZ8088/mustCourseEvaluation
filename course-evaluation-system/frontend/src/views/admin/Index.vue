<template>
  <div class="admin-container">
    <div class="admin-sidebar">
      <h2>管理控制台</h2>
      <ul class="admin-menu">
        <li>
          <router-link to="/admin/dashboard">控制面板</router-link>
        </li>
        <li>
          <router-link to="/admin/courses">课程管理</router-link>
        </li>
        <li>
          <router-link to="/admin/teachers">教师管理</router-link>
        </li>
        <li>
          <router-link to="/admin/faculties">院系管理</router-link>
        </li>
        <li>
          <router-link to="/admin/reviews">评价管理</router-link>
        </li>
        <li>
          <router-link to="/admin/users">用户管理</router-link>
        </li>
        <li class="separator"></li>
        <li>
          <router-link to="/" class="home-link">
            <i class="fas fa-home"></i> 返回首页
          </router-link>
        </li>
      </ul>
    </div>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminIndex'
}
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  padding: 20px 0;
}

.admin-sidebar h2 {
  text-align: center;
  margin-bottom: 30px;
  padding: 0 20px;
}

.admin-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.admin-menu li {
  margin-bottom: 5px;
}

.separator {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
  margin: 15px 20px;
}

.admin-menu a {
  display: block;
  padding: 12px 20px;
  color: white;
  text-decoration: none;
  transition: background-color 0.3s;
}

.admin-menu a:hover, 
.admin-menu a.router-link-active {
  background-color: rgba(255, 255, 255, 0.1);
}

.home-link {
  color: #ffd700 !important;
}

.admin-content {
  flex: 1;
  padding: 30px;
  background-color: #f8f9fa;
}
</style> 