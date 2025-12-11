import http from './http.service';

/**
 * 推荐服务 - 调用后端API实现AI推荐和对话管理
 * 
 * 所有对话数据存储在数据库中，通过后端 API 进行 CRUD 操作
 */
class RecommendationService {
  constructor() {
    this.aiServiceAvailable = null;
  }

  // ==================== AI 服务状态 ====================

  /**
   * 检查AI服务是否可用
   */
  async checkAIServiceStatus() {
    try {
      const response = await http.get('/ai-recommendations/status');
      this.aiServiceAvailable = response.data.available;
      return response.data;
    } catch (error) {
      console.error('检查AI服务状态失败:', error);
      this.aiServiceAvailable = false;
      return { available: false, message: '无法连接到后端服务' };
    }
  }

  /**
   * 获取服务状态
   */
  async getServiceStatus() {
    const status = await this.checkAIServiceStatus();
    return {
      aiEnabled: status.available,
      mode: status.available ? 'AI增强模式' : '服务未就绪'
    };
  }

  // ==================== 对话管理（数据库存储） ====================

  /**
   * 获取对话历史列表
   */
  async getConversationHistory() {
    try {
      const response = await http.get('/ai-recommendations/conversations');
      // 转换为前端需要的格式
      return response.data.map(conv => ({
        id: conv.conversationId,
        title: conv.title,
        createdAt: conv.createdAt,
        updatedAt: conv.updatedAt,
        context: conv.context || {},
        messages: conv.messages || []
      }));
    } catch (error) {
      console.error('获取对话历史失败:', error);
      return [];
    }
  }

  /**
   * 创建新对话
   */
  async createNewConversation() {
    try {
      const conversationId = `conv_${Date.now()}`;
      const response = await http.post('/ai-recommendations/conversations', { conversationId });
      return {
        id: response.data.conversationId,
        title: response.data.title,
        createdAt: response.data.createdAt,
        context: response.data.context || {},
        messages: response.data.messages || []
      };
    } catch (error) {
      console.error('创建对话失败:', error);
      throw error;
    }
  }

  /**
   * 获取单个对话详情（包含消息）
   */
  async getConversation(conversationId) {
    try {
      const response = await http.get(`/ai-recommendations/conversations/${conversationId}`);
      return {
        id: response.data.conversationId,
        title: response.data.title,
        createdAt: response.data.createdAt,
        updatedAt: response.data.updatedAt,
        context: response.data.context || {},
        messages: (response.data.messages || []).map(msg => ({
          id: msg.messageId,
          role: msg.role,
          content: msg.content,
          type: msg.messageType,
          courses: msg.courses,
          timestamp: msg.createdAt
        }))
      };
    } catch (error) {
      console.error('获取对话失败:', error);
      return null;
    }
  }

  /**
   * 添加消息到对话
   */
  async addMessageToConversation(conversationId, message) {
    try {
      const messageDto = {
        messageId: message.id || `msg_${Date.now()}`,
        role: message.role,
        content: message.content,
        messageType: message.type || 'text',
        courses: message.courses || []
      };
      
      await http.post(`/ai-recommendations/conversations/${conversationId}/messages`, messageDto);
      return true;
    } catch (error) {
      console.error('保存消息失败:', error);
      return false;
    }
  }

  /**
   * 更新对话标题
   */
  async updateConversationTitle(conversationId, title) {
    try {
      // 截断标题
      const truncatedTitle = title.length > 20 ? title.substring(0, 20) + '...' : title;
      await http.put(`/ai-recommendations/conversations/${conversationId}/title`, { title: truncatedTitle });
      return true;
    } catch (error) {
      console.error('更新标题失败:', error);
      return false;
    }
  }

  /**
   * 更新对话上下文
   */
  async updateConversationContext(conversationId, context) {
    try {
      await http.put(`/ai-recommendations/conversations/${conversationId}/context`, context);
      return true;
    } catch (error) {
      console.error('更新上下文失败:', error);
      return false;
    }
  }

  /**
   * 删除对话
   */
  async deleteConversation(conversationId) {
    try {
      await http.delete(`/ai-recommendations/conversations/${conversationId}`);
      return true;
    } catch (error) {
      console.error('删除对话失败:', error);
      return false;
    }
  }

  /**
   * 清空所有对话
   */
  async clearAllHistory() {
    try {
      await http.delete('/ai-recommendations/conversations');
      return true;
    } catch (error) {
      console.error('清空历史失败:', error);
      return false;
    }
  }

  // ==================== AI 聊天 ====================

  /**
   * 发送消息并获取AI回复
   * @param {string} conversationId - 对话ID
   * @param {string} userMessage - 用户消息
   * @param {object} conversationContext - 对话上下文（提取的参数）
   * @param {array} conversationHistory - 对话历史消息
   */
  async sendMessage(conversationId, userMessage, conversationContext, conversationHistory = []) {
    try {
      console.log('[前端] 发送消息到后端:', userMessage);
      console.log('[前端] 对话历史条数:', conversationHistory.length);
      
      const response = await http.post('/ai-recommendations/chat', {
        message: userMessage,
        context: conversationContext || {},
        conversationId: conversationId,
        conversationHistory: conversationHistory.map(msg => ({
          role: msg.role,
          content: msg.content
        }))
      });

      const result = response.data;
      console.log('[前端] 收到后端响应:', result);

      // 返回标准格式
      if (result.type === 'recommendation' && result.courses) {
        return {
          type: 'recommendation',
          content: result.content,
          courses: result.courses.map(course => ({
            id: course.id,
            code: course.code,
            name: course.name,
            credits: course.credits,
            type: course.type,
            description: course.description,
            facultyName: course.facultyName,
            teacherName: course.teacherName,
            averageRating: course.averageRating,
            reviewCount: course.reviewCount,
            reason: course.reason
          })),
          updatedContext: result.updatedContext
        };
      } else {
        return {
          type: 'text',
          content: result.content || result.error || '抱歉，处理您的请求时出现了问题。',
          updatedContext: result.updatedContext
        };
      }

    } catch (error) {
      console.error('[前端] 调用后端AI推荐API失败:', error);
      console.error('[前端] 错误详情:', error.response?.data || error.message);
      
      // 根据不同错误类型返回不同提示
      let errorMessage = '抱歉，AI推荐服务暂时不可用。';
      if (error.response?.status === 401) {
        errorMessage = '您的登录已过期，请重新登录后再试。';
      } else if (error.response?.status === 500) {
        errorMessage = '服务器处理请求时出错，请稍后再试。';
      } else if (error.code === 'ECONNABORTED') {
        errorMessage = 'AI请求超时，请稍后再试。';
      } else if (!error.response) {
        errorMessage = '无法连接到服务器，请检查网络连接。';
      }
      
      return {
        type: 'text',
        content: errorMessage
      };
    }
  }

  // ==================== 辅助方法 ====================

  /**
   * 分析用户输入以提取参数
   */
  analyzeUserInput(message, context) {
    const intent = this._extractIntentByRules(message, context);
    return intent.parameters;
  }

  /**
   * 基于规则提取意图
   */
  _extractIntentByRules(message, context) {
    const intent = {
      parameters: {
        courseType: context.courseType || null,
        credits: context.credits || null,
        keywords: context.keywords || [],
        difficulty: context.difficulty || null,
        faculty: context.faculty || null,
        teacher: context.teacher || null
      }
    };

    const lowerMessage = message.toLowerCase();

    if (lowerMessage.includes('必修') || lowerMessage.includes('必修课')) {
      intent.parameters.courseType = 'COMPULSORY';
    } else if (lowerMessage.includes('选修') || lowerMessage.includes('选修课')) {
      intent.parameters.courseType = 'ELECTIVE';
    }

    const creditMatch = message.match(/(\d+)\s*学分/);
    if (creditMatch) {
      intent.parameters.credits = parseInt(creditMatch[1]);
    }

    if (lowerMessage.includes('简单') || lowerMessage.includes('容易') || lowerMessage.includes('轻松')) {
      intent.parameters.difficulty = 'easy';
    } else if (lowerMessage.includes('难') || lowerMessage.includes('有挑战')) {
      intent.parameters.difficulty = 'hard';
    }

    const faculties = ['创新工程学院', '商学院', '人文艺术学院', '酒店与旅游管理学院', '医学院'];
    for (const faculty of faculties) {
      if (message.includes(faculty)) {
        intent.parameters.faculty = faculty;
        break;
      }
    }

    const teachers = ['陈伟', '林晓明', '黄建华', '周梅', '王艺琳', '刘芳', '张红', '李强', '赵明德', '孙丽丽'];
    for (const teacher of teachers) {
      if (message.includes(teacher)) {
        intent.parameters.teacher = teacher;
        break;
      }
    }

    return intent;
  }

  /**
   * 延迟函数
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

export default new RecommendationService();
