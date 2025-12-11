<template>
  <div class="recommendations-container">
    <!-- 顶部标题区 -->
    <div class="page-header">
      <h1>AI课程推荐</h1>
      <div class="header-right">
        <span v-if="isLoggedIn" :class="['status-badge', serviceStatus.aiEnabled ? 'ai-mode' : 'rule-mode']">
          <i :class="serviceStatus.aiEnabled ? 'fas fa-robot' : 'fas fa-cog'"></i>
          {{ serviceStatus.mode }}
        </span>
        <router-link to="/" class="btn-back">
          <i class="fas fa-home"></i> 返回首页
        </router-link>
      </div>
    </div>

    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn" class="login-required">
      <div class="login-prompt">
        <i class="fas fa-lock prompt-icon"></i>
        <h2>需要登录</h2>
        <p>请先登录后再使用 AI 课程推荐功能</p>
        <p class="feature-hint">登录后您可以：</p>
        <ul class="feature-list">
          <li><i class="fas fa-check"></i> 获取个性化课程推荐</li>
          <li><i class="fas fa-check"></i> 保存对话历史记录</li>
          <li><i class="fas fa-check"></i> 与 AI 进行多轮对话</li>
        </ul>
        <router-link to="/login" class="btn-login">
          <i class="fas fa-sign-in-alt"></i> 前往登录
        </router-link>
        <p class="register-hint">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>

    <!-- 已登录：显示正常内容 -->
    <template v-else>
      <!-- AI服务状态提示（仅当服务不可用时显示简单提示） -->
      <div v-if="!serviceStatus.aiEnabled && showApiWarning" class="service-notice">
        <div class="notice-content">
          <i class="fas fa-info-circle"></i>
          <span>AI推荐服务暂时不可用，请稍后再试或联系管理员。</span>
          <button @click="showApiWarning = false" class="notice-close">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>

      <div class="content-wrapper">
      <!-- 侧边栏：历史对话列表 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <button @click="createNewConversation" class="btn-new">
            <i class="fas fa-plus"></i> 新建对话
          </button>
        </div>
        
        <div class="conversation-list">
          <div 
            v-for="conv in conversationHistory" 
            :key="conv.id"
            :class="['conversation-item', { active: conv.id === currentConversationId }]"
            @click="switchConversation(conv.id)"
          >
            <div class="conversation-title">{{ conv.title }}</div>
            <div class="conversation-date">{{ formatDate(conv.createdAt) }}</div>
          </div>
          
          <div v-if="conversationHistory.length === 0" class="no-history">
            <p>暂无历史对话</p>
          </div>
        </div>
        
        <button 
          v-if="conversationHistory.length > 0" 
          @click="clearAllHistory" 
          class="btn-clear"
        >
          <i class="fas fa-trash"></i> 清空历史
        </button>
      </div>

      <!-- 主对话区 -->
      <div class="chat-area">
        <div class="messages-container" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-message">
            <h2>👋 欢迎使用AI课程推荐系统</h2>
            <p>我可以根据您的需求为您推荐合适的课程。</p>
            <p>请告诉我您的需求，例如：</p>
            <ul>
              <li>"我想学习编程相关的课程"</li>
              <li>"推荐一些3学分的选修课"</li>
              <li>"有哪些简单易学的必修课？"</li>
            </ul>
          </div>

          <!-- 消息列表 -->
          <div v-for="message in messages" :key="message.id" :class="['message', message.role]">
            <div class="message-content">
              <!-- 用户消息：纯文本 -->
              <div v-if="message.role === 'user'" class="message-text">{{ message.content }}</div>
              <!-- AI消息：Markdown渲染 -->
              <div v-else class="message-text markdown-body" v-html="renderMarkdown(message.content)"></div>
              
              <!-- 课程推荐卡片 -->
              <div v-if="message.type === 'recommendation' && message.courses" class="recommended-courses">
                <div 
                  v-for="course in message.courses" 
                  :key="course.id"
                  class="course-card"
                  @click="viewCourseDetails(course.id)"
                >
                  <div class="course-header">
                    <span class="course-code">{{ course.code }}</span>
                    <span :class="['course-type', course.type === 'COMPULSORY' ? 'compulsory' : 'elective']">
                      {{ course.type === 'COMPULSORY' ? '必修' : '选修' }}
                    </span>
                  </div>
                  <h4>{{ course.name }}</h4>
                  <p class="course-faculty">{{ course.facultyName }}</p>
                  <p class="course-teacher" v-if="course.teacherName">
                    授课教师: {{ course.teacherName }}
                  </p>
                  <p class="course-credits">学分: {{ course.credits }}</p>
                  <p class="course-rating" v-if="course.averageRating">
                    <span class="stars">
                      <i v-for="n in 5" :key="n" class="fas" 
                        :class="n <= Math.round(course.averageRating) ? 'fa-star' : 'fa-star-o'"></i>
                    </span>
                    <span class="rating-value">{{ course.averageRating.toFixed(1) }}</span>
                  </p>
                  <p class="recommendation-reason" v-if="course.reason">
                    <i class="fas fa-lightbulb"></i> {{ course.reason }}
                  </p>
                </div>
              </div>
              
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>

          <!-- 加载中提示 -->
          <div v-if="isTyping" class="message ai typing-indicator">
            <div class="message-content">
              <div class="typing-info">
                <div class="typing-dots">
                  <span></span><span></span><span></span>
                </div>
                <span class="typing-text">{{ typingMessage }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <input 
            v-model="userInput"
            @keypress.enter="sendMessage"
            type="text"
            placeholder="输入您的需求..."
            :disabled="isTyping"
            class="message-input"
          />
          <button 
            @click="sendMessage" 
            :disabled="!userInput.trim() || isTyping"
            class="btn-send"
          >
            <i class="fas fa-paper-plane"></i> 发送
          </button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex';
import recommendationService from '@/services/recommendation.service';
import MarkdownIt from 'markdown-it';

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: false,        // 禁用 HTML 标签（安全）
  breaks: true,       // 将换行符转为 <br>
  linkify: true,      // 自动转换 URL 为链接
  typographer: true   // 启用一些语言增强
});

export default {
  name: 'Recommendations',
  data() {
    return {
      userInput: '',
      serviceStatus: {
        aiEnabled: false,
        mode: '加载中...'
      },
      showApiWarning: true
    };
  },
  computed: {
    ...mapState('recommendations', ['currentConversationId', 'messages', 'isTyping']),
    ...mapGetters('recommendations', ['conversationHistory']),
    
    isLoggedIn() {
      return this.$store.getters['auth/isLoggedIn'];
    },
    
    typingMessage() {
      if (!this.serviceStatus.aiEnabled) {
        return '正在分析您的需求...';
      }
      // 可以根据不同阶段显示不同提示
      return 'AI正在思考...';
    }
  },
  async mounted() {
    // 只有登录用户才加载对话数据
    if (this.isLoggedIn) {
      this.checkServiceStatus();
      await this.refreshConversations();  // 初始化对话列表（从数据库加载）
      await this.initConversation();
    }
  },
  methods: {
    ...mapActions('recommendations', [
      'sendUserMessage',
      'createConversation',
      'switchToConversation',
      'clearHistory',
      'refreshConversations'
    ]),
    
    /**
     * 检查服务状态（调用后端API）
     */
    async checkServiceStatus() {
      try {
        this.serviceStatus = await recommendationService.getServiceStatus();
      } catch (error) {
        console.error('检查服务状态失败:', error);
        this.serviceStatus = {
          aiEnabled: false,
          mode: '连接中...'
        };
      }
    },
    
    async initConversation() {
      // 如果没有当前对话，尝试恢复最近的历史对话
      if (!this.currentConversationId) {
        const history = this.conversationHistory;
        if (history && history.length > 0) {
          // 恢复最近的对话
          await this.switchToConversation(history[0].id);
        }
        // 如果没有历史对话，不自动创建，等待用户操作
      }
    },
    
    async sendMessage() {
      if (!this.userInput.trim() || this.isTyping) return;
      
      const message = this.userInput.trim();
      this.userInput = '';
      
      await this.sendUserMessage(message);
      
      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    
    async createNewConversation() {
      await this.createConversation();
      this.scrollToBottom();
    },
    
    async switchConversation(conversationId) {
      try {
        await this.switchToConversation(conversationId);
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      } catch (error) {
        console.error('切换对话失败:', error);
        // 不显示alert，因为Vuex已经处理了降级
      }
    },
    
    async clearAllHistory() {
      if (confirm('确定要清空所有历史对话吗？此操作不可恢复。')) {
        await this.clearHistory();
      }
    },
    
    viewCourseDetails(courseId) {
      this.$router.push(`/courses/${courseId}`);
    },
    
    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },
    
    formatDate(dateString) {
      const date = new Date(dateString);
      const now = new Date();
      const diffTime = Math.abs(now - date);
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays === 0) {
        return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      } else if (diffDays === 1) {
        return '昨天';
      } else if (diffDays < 7) {
        return diffDays + '天前';
      } else {
        return date.toLocaleDateString('zh-CN');
      }
    },
    
    formatTime(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    },
    
    /**
     * 渲染 Markdown 内容为 HTML
     */
    renderMarkdown(content) {
      if (!content) return '';
      return md.render(content);
    }
  }
};
</script>

<style scoped>
.recommendations-container {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h1 {
  margin: 0;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.ai-mode {
  background-color: #e3f2fd;
  color: #1976d2;
}

.status-badge.rule-mode {
  background-color: #fff3e0;
  color: #f57c00;
}

.btn-back {
  display: inline-block;
  background-color: #0066cc;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #0055aa;
}

/* 服务状态提示 */
.service-notice {
  margin-bottom: 20px;
  background-color: #e3f2fd;
  border: 1px solid #90caf9;
  border-radius: 8px;
  padding: 12px 15px;
}

.notice-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-content > i {
  color: #1976d2;
  font-size: 18px;
}

.notice-content > span {
  flex: 1;
  color: #1565c0;
  font-size: 14px;
}

.notice-close {
  background: none;
  border: none;
  color: #1976d2;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 14px;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.notice-close:hover {
  opacity: 1;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  flex: 1;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  margin-bottom: 15px;
}

.sidebar-header h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.btn-new {
  width: 100%;
  padding: 10px;
  background-color: #4a6bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.btn-new:hover {
  background-color: #3955d9;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 15px;
}

.conversation-item {
  padding: 12px;
  margin-bottom: 8px;
  background-color: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.conversation-item:hover {
  background-color: #e8e8e8;
}

.conversation-item.active {
  background-color: #4a6bff;
  color: white;
}

.conversation-title {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
}

.conversation-date {
  font-size: 12px;
  opacity: 0.7;
}

.no-history {
  text-align: center;
  padding: 20px;
  color: #999;
}

.btn-clear {
  width: 100%;
  padding: 10px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.btn-clear:hover {
  background-color: #c82333;
}

/* 聊天区域样式 */
.chat-area {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f8f9fa;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.welcome-message h2 {
  color: #333;
  margin-bottom: 20px;
}

.welcome-message ul {
  text-align: left;
  display: inline-block;
  margin-top: 15px;
}

.welcome-message li {
  margin-bottom: 8px;
  color: #555;
}

.message {
  margin-bottom: 20px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
}

.message.user .message-content {
  background-color: #4a6bff;
  color: white;
}

.message.ai .message-content {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  color: #333;
}

.message-text {
  margin-bottom: 5px;
  line-height: 1.5;
}

/* Markdown 渲染样式 */
.markdown-body :deep(p) {
  margin: 0 0 10px 0;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: #333;
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 5px 0;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3), .markdown-body :deep(h4) {
  margin: 15px 0 10px 0;
  font-weight: 600;
  color: #333;
}

.markdown-body :deep(h1) { font-size: 1.4em; }
.markdown-body :deep(h2) { font-size: 1.2em; }
.markdown-body :deep(h3) { font-size: 1.1em; }
.markdown-body :deep(h4) { font-size: 1em; }

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 15px 0;
}

.markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 10px 15px;
  border-left: 4px solid #4a6bff;
  background-color: #f5f7ff;
  color: #555;
}

.markdown-body :deep(code) {
  background-color: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(a) {
  color: #0066cc;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.markdown-body :deep(th), .markdown-body :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background-color: #f5f5f5;
  font-weight: 600;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 5px;
}

/* 推荐课程卡片 */
.recommended-courses {
  margin-top: 15px;
  display: grid;
  gap: 15px;
}

.course-card {
  background-color: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.course-code {
  font-weight: bold;
  color: #555;
  font-size: 13px;
}

.course-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  color: white;
}

.course-type.compulsory {
  background-color: #e53935;
}

.course-type.elective {
  background-color: #43a047;
}

.course-card h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.course-faculty {
  color: #0066cc;
  margin: 4px 0;
  font-size: 13px;
}

.course-teacher,
.course-credits {
  color: #666;
  margin: 4px 0;
  font-size: 13px;
}

.course-rating {
  display: flex;
  align-items: center;
  margin: 8px 0;
}

.stars {
  color: #ffc107;
  margin-right: 5px;
  font-size: 14px;
}

.rating-value {
  font-weight: bold;
  font-size: 13px;
  color: #333;
}

.recommendation-reason {
  margin-top: 10px;
  padding: 8px;
  background-color: #fff3cd;
  border-left: 3px solid #ffc107;
  border-radius: 4px;
  font-size: 13px;
  color: #856404;
}

.recommendation-reason i {
  margin-right: 5px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
}

.typing-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background-color: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

.typing-text {
  color: #999;
  font-size: 13px;
  font-style: italic;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* 输入区域 */
.input-area {
  display: flex;
  gap: 10px;
  padding: 20px;
  background-color: #fff;
  border-top: 1px solid #e0e0e0;
}

.message-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  border-color: #4a6bff;
}

.message-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.btn-send {
  padding: 12px 24px;
  background-color: #4a6bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
  white-space: nowrap;
}

.btn-send:hover:not(:disabled) {
  background-color: #3955d9;
}

.btn-send:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 未登录提示样式 */
.login-required {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.login-prompt {
  background: white;
  border-radius: 16px;
  padding: 50px 60px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.prompt-icon {
  font-size: 64px;
  color: #4a6bff;
  margin-bottom: 20px;
  display: block;
}

.login-prompt h2 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 28px;
}

.login-prompt > p {
  color: #666;
  margin-bottom: 20px;
  font-size: 16px;
}

.feature-hint {
  color: #555;
  font-weight: 500;
  margin-bottom: 10px !important;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 auto 30px auto;
  text-align: left;
  display: table;
}

.feature-list li {
  color: #666;
  margin: 10px 0;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.feature-list li i {
  color: #43a047;
  margin-right: 10px;
  flex-shrink: 0;
}

.btn-login {
  display: inline-block;
  background: linear-gradient(135deg, #4a6bff 0%, #6b8bff 100%);
  color: white;
  padding: 14px 50px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(74, 107, 255, 0.3);
  margin-top: 10px;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 107, 255, 0.4);
}

.btn-login i {
  margin-right: 8px;
}

.register-hint {
  margin-top: 20px;
  color: #888;
  font-size: 14px;
}

.register-hint a {
  color: #4a6bff;
  text-decoration: none;
  font-weight: 500;
}

.register-hint a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    max-height: 200px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .login-prompt {
    padding: 30px 20px;
  }
  
  .prompt-icon {
    font-size: 48px;
  }
  
  .login-prompt h2 {
    font-size: 22px;
  }
}
</style>

