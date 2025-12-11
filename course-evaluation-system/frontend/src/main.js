import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 创建一个加载外部资源的函数
function loadExternalResource(url, type) {
  return new Promise((resolve, reject) => {
    let tag;
    
    if (type === 'css') {
      tag = document.createElement('link');
      tag.rel = 'stylesheet';
      tag.href = url;
    }
    else if (type === 'js') {
      tag = document.createElement('script');
      tag.src = url;
    }
    
    if (tag) {
      tag.onload = () => resolve(url);
      tag.onerror = () => reject(url);
      document.head.appendChild(tag);
    }
  });
}

// 加载Font Awesome
loadExternalResource('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css', 'css')
  .catch(error => console.error('无法加载Font Awesome', error));

const app = createApp(App)

app.use(store)
   .use(router)
   .use(ElementPlus, {
      locale: zhCn,
      size: 'default'
   })
   .mount('#app') 