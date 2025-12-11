<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在。</p>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.not-found-content {
  text-align: center;
  padding: 50px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

h1 {
  font-size: 8rem;
  margin: 0;
  color: #4a6bff;
  line-height: 1;
}

h2 {
  font-size: 2rem;
  margin-bottom: 20px;
  color: #333;
}

p {
  color: #666;
  margin-bottom: 30px;
  font-size: 1.1rem;
}

.btn {
  display: inline-block;
  padding: 12px 30px;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #4a6bff;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #3955d9;
}
</style> 