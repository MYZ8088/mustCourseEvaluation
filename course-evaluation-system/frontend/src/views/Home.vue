<template>
  <div class="home-container">
    <div class="welcome-section">
      <img src="../assets/logo.png" alt="校徽" class="school-logo">
      <h1>欢迎使用课程评价系统</h1>
      <p class="subtitle">帮助学生获取课程评价和推荐课程</p>
    </div>
    
    <div class="features-section">
      <div class="feature-card">
        <h3>课程评价</h3>
        <p>查看和评价各种课程，帮助其他学生做出更好的选择。</p>
        <router-link to="/courses" class="btn btn-primary">浏览课程</router-link>
      </div>
      
      <div class="feature-card">
        <h3>教师评价</h3>
        <p>了解不同教师的教学风格和特点，找到最适合你的老师。</p>
        <router-link to="/teachers" class="btn btn-primary">查看教师</router-link>
      </div>
      
      <div class="feature-card">
        <h3>AI课程推荐</h3>
        <p>通过AI智能对话，为你推荐最合适的课程。</p>
        <router-link to="/recommendations" class="btn btn-primary">开始推荐</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.home-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 50px;
  padding: 50px 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.school-logo {
  width: 120px;
  height: 120px;
  margin-bottom: 20px;
}

h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 20px;
}

.subtitle {
  font-size: 1.2rem;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.features-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.feature-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.feature-card p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.btn {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #4a6bff;
  color: white;
}

.btn-primary:hover {
  background-color: #3955d9;
}
</style> 