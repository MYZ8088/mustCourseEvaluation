import recommendationService from '@/services/recommendation.service';

const state = {
  currentConversationId: null,
  messages: [],
  isTyping: false,
  error: null,
  conversations: [],  // 对话历史列表
  isFirstUserMessage: false  // 标记是否是第一条用户消息
};

const getters = {
  conversationHistory: (state) => state.conversations,
  currentMessages: (state) => state.messages,
  isAITyping: (state) => state.isTyping,
  currentConversationId: (state) => state.currentConversationId
};

const mutations = {
  SET_CURRENT_CONVERSATION(state, conversationId) {
    state.currentConversationId = conversationId;
  },
  
  SET_MESSAGES(state, messages) {
    state.messages = messages;
  },
  
  ADD_MESSAGE(state, message) {
    state.messages.push(message);
  },
  
  SET_TYPING(state, isTyping) {
    state.isTyping = isTyping;
  },
  
  CLEAR_MESSAGES(state) {
    state.messages = [];
  },
  
  SET_ERROR(state, error) {
    state.error = error;
  },
  
  CLEAR_ERROR(state) {
    state.error = null;
  },
  
  SET_CONVERSATIONS(state, conversations) {
    state.conversations = conversations;
  },
  
  SET_FIRST_USER_MESSAGE(state, isFirst) {
    state.isFirstUserMessage = isFirst;
  }
};

const actions = {
  /**
   * 刷新对话历史列表（从数据库获取）
   */
  async refreshConversations({ commit }) {
    try {
      const history = await recommendationService.getConversationHistory();
      commit('SET_CONVERSATIONS', history);
    } catch (error) {
      console.error('刷新对话列表失败:', error);
      commit('SET_CONVERSATIONS', []);
    }
  },
  
  /**
   * 创建新对话
   */
  async createConversation({ commit, dispatch }) {
    try {
      const conversation = await recommendationService.createNewConversation();
      commit('SET_CURRENT_CONVERSATION', conversation.id);
      commit('SET_MESSAGES', []);
      commit('SET_FIRST_USER_MESSAGE', true);  // 标记下一条用户消息为第一条
      await dispatch('refreshConversations');
      return conversation;
    } catch (error) {
      console.error('创建对话失败:', error);
      commit('SET_ERROR', error.message);
      throw error;
    }
  },
  
  /**
   * 切换到指定对话
   */
  async switchToConversation({ commit, dispatch }, conversationId) {
    try {
      commit('CLEAR_ERROR');
      const conversation = await recommendationService.getConversation(conversationId);
      
      if (conversation) {
        commit('SET_CURRENT_CONVERSATION', conversationId);
        commit('SET_MESSAGES', conversation.messages || []);
        // 检查是否已有用户消息
        const hasUserMessage = conversation.messages && conversation.messages.some(m => m.role === 'user');
        commit('SET_FIRST_USER_MESSAGE', !hasUserMessage);
      } else {
        console.warn(`对话 ${conversationId} 不存在或已损坏`);
        await recommendationService.deleteConversation(conversationId);
        commit('SET_CURRENT_CONVERSATION', null);
        commit('CLEAR_MESSAGES');
        await dispatch('refreshConversations');
      }
    } catch (error) {
      console.error('切换对话失败:', error);
      commit('SET_ERROR', error.message);
      commit('SET_CURRENT_CONVERSATION', null);
      commit('CLEAR_MESSAGES');
    }
  },
  
  /**
   * 发送用户消息
   */
  async sendUserMessage({ state, commit, dispatch }, messageContent) {
    try {
      commit('CLEAR_ERROR');
      
      // 如果没有当前对话，创建一个新对话
      if (!state.currentConversationId) {
        await dispatch('createConversation');
      }
      
      // 创建用户消息
      const userMessage = {
        id: `msg_${Date.now()}`,
        role: 'user',
        content: messageContent,
        timestamp: new Date().toISOString(),
        type: 'text'
      };
      
      // 添加到界面
      commit('ADD_MESSAGE', userMessage);
      
      // 保存用户消息到数据库
      await recommendationService.addMessageToConversation(
        state.currentConversationId, 
        userMessage
      );
      
      // 如果是第一条用户消息，更新对话标题
      if (state.isFirstUserMessage) {
        await recommendationService.updateConversationTitle(
          state.currentConversationId,
          messageContent
        );
        commit('SET_FIRST_USER_MESSAGE', false);
      }
      
      // 刷新对话列表（标题已更新）
      await dispatch('refreshConversations');
      
      // 设置AI正在输入
      commit('SET_TYPING', true);
      
      // 获取当前对话的上下文
      const conversation = await recommendationService.getConversation(state.currentConversationId);
      const context = conversation ? conversation.context : {};
      
      // 构建对话历史（包含刚添加的用户消息）
      const conversationHistory = [...state.messages];
      
      // 获取AI回复（传递对话历史实现上下文记忆）
      const aiResponse = await recommendationService.sendMessage(
        state.currentConversationId,
        messageContent,
        context,
        conversationHistory  // 传递对话历史
      );
      
      // 更新对话上下文
      if (aiResponse.updatedContext) {
        await recommendationService.updateConversationContext(
          state.currentConversationId, 
          aiResponse.updatedContext
        );
      }
      
      // 创建AI消息
      const aiMessage = {
        id: `msg_${Date.now() + 1}`,
        role: 'ai',
        content: aiResponse.content,
        timestamp: new Date().toISOString(),
        type: aiResponse.type,
        courses: aiResponse.courses || null
      };
      
      // 停止AI输入状态
      commit('SET_TYPING', false);
      
      // 添加AI回复到界面
      commit('ADD_MESSAGE', aiMessage);
      
      // 保存AI消息到数据库
      await recommendationService.addMessageToConversation(
        state.currentConversationId,
        aiMessage
      );
      
      return aiMessage;
    } catch (error) {
      console.error('发送消息失败:', error);
      commit('SET_TYPING', false);
      commit('SET_ERROR', error.message);
      
      const errorMessage = {
        id: `msg_${Date.now()}`,
        role: 'ai',
        content: '抱歉，处理您的消息时出现了问题。请稍后再试。',
        timestamp: new Date().toISOString(),
        type: 'text'
      };
      commit('ADD_MESSAGE', errorMessage);
      
      throw error;
    }
  },
  
  /**
   * 清空历史记录
   */
  async clearHistory({ commit, dispatch }) {
    try {
      await recommendationService.clearAllHistory();
      commit('SET_CURRENT_CONVERSATION', null);
      commit('CLEAR_MESSAGES');
      await dispatch('refreshConversations');
      return true;
    } catch (error) {
      console.error('清空历史失败:', error);
      commit('SET_ERROR', error.message);
      throw error;
    }
  },
  
  /**
   * 删除指定对话
   */
  async deleteConversation({ state, commit, dispatch }, conversationId) {
    try {
      await recommendationService.deleteConversation(conversationId);
      await dispatch('refreshConversations');
      
      // 如果删除的是当前对话，切换到其他对话
      if (state.currentConversationId === conversationId) {
        const history = await recommendationService.getConversationHistory();
        if (history && history.length > 0) {
          await dispatch('switchToConversation', history[0].id);
        } else {
          commit('SET_CURRENT_CONVERSATION', null);
          commit('CLEAR_MESSAGES');
        }
      }
      
      return true;
    } catch (error) {
      console.error('删除对话失败:', error);
      commit('SET_ERROR', error.message);
      throw error;
    }
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
