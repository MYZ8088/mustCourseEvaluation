import http from './http.service';

/**
 * 对话管理API服务
 * 与后端数据库交互，实现对话持久化
 */
class ConversationService {
  
  /**
   * 获取用户的对话历史
   */
  async getConversations() {
    return http.get('/ai-recommendations/conversations');
  }
  
  /**
   * 创建新对话
   */
  async createConversation(conversationId) {
    return http.post('/ai-recommendations/conversations', {
      conversationId
    });
  }
  
  /**
   * 获取对话详情（包含所有消息）
   */
  async getConversation(conversationId) {
    return http.get(`/ai-recommendations/conversations/${conversationId}`);
  }
  
  /**
   * 保存消息到对话
   */
  async saveMessage(conversationId, message) {
    return http.post(`/ai-recommendations/conversations/${conversationId}/messages`, message);
  }
  
  /**
   * 更新对话上下文
   */
  async updateContext(conversationId, context) {
    return http.put(`/ai-recommendations/conversations/${conversationId}/context`, context);
  }
  
  /**
   * 更新对话标题
   */
  async updateTitle(conversationId, title) {
    return http.put(`/ai-recommendations/conversations/${conversationId}/title`, {
      title
    });
  }
  
  /**
   * 删除对话
   */
  async deleteConversation(conversationId) {
    return http.delete(`/ai-recommendations/conversations/${conversationId}`);
  }
  
  /**
   * 清空所有对话
   */
  async clearAllConversations() {
    return http.delete('/ai-recommendations/conversations');
  }
  
  /**
   * 批量保存对话（用于数据迁移）
   */
  async batchCreateConversations(conversations) {
    const results = [];
    for (const conv of conversations) {
      try {
        // 创建对话
        const convResult = await this.createConversation(conv.id);
        
        // 更新标题
        if (conv.title && conv.title !== '新对话') {
          await this.updateTitle(conv.id, conv.title);
        }
        
        // 更新上下文
        if (conv.context && Object.keys(conv.context).length > 0) {
          await this.updateContext(conv.id, conv.context);
        }
        
        // 保存所有消息
        if (conv.messages && conv.messages.length > 0) {
          for (const msg of conv.messages) {
            await this.saveMessage(conv.id, {
              messageId: msg.id,
              role: msg.role,
              content: msg.content,
              messageType: msg.type || 'text',
              courses: msg.courses || []
            });
          }
        }
        
        results.push({ success: true, conversationId: conv.id });
      } catch (error) {
        console.error(`迁移对话 ${conv.id} 失败:`, error);
        results.push({ success: false, conversationId: conv.id, error: error.message });
      }
    }
    return results;
  }
}

export default new ConversationService();





















