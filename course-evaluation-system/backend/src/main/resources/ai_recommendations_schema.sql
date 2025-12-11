-- AI课程推荐系统数据库表结构
-- 作者：AI Assistant
-- 创建日期：2025-11-28

-- 表1: ai_conversations - 对话记录表
CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(50) UNIQUE NOT NULL,  -- 前端生成的UUID
    user_id BIGINT REFERENCES users(id),          -- 关联用户ID
    title VARCHAR(100) DEFAULT '新对话',
    context JSONB,                                 -- 对话上下文（课程类型、学分等）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_conversations_user_id ON ai_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_created_at ON ai_conversations(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_conversation_id ON ai_conversations(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_user_created ON ai_conversations(user_id, created_at DESC);

-- 表2: ai_messages - 消息记录表
CREATE TABLE IF NOT EXISTS ai_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(50) UNIQUE NOT NULL,       -- 前端生成的UUID
    conversation_id BIGINT REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,                     -- 'user' 或 'ai'
    content TEXT NOT NULL,                         -- 消息内容
    message_type VARCHAR(20) DEFAULT 'text',       -- 'text' 或 'recommendation'
    courses JSONB,                                 -- 推荐的课程数据（仅recommendation类型）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_messages_conversation_id ON ai_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_messages_created_at ON ai_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_messages_message_id ON ai_messages(message_id);

-- 表3: user_preferences - 用户偏好表（可选，用于个性化推荐）
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) UNIQUE,
    preferred_course_type VARCHAR(20),             -- 'COMPULSORY' 或 'ELECTIVE'
    preferred_credits INTEGER,                     -- 偏好学分
    preferred_difficulty VARCHAR(20),              -- 'easy', 'medium', 'hard'
    interest_keywords TEXT[],                      -- 兴趣关键词数组
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);

-- Add comments
COMMENT ON TABLE ai_conversations IS 'AI course recommendation conversations';
COMMENT ON TABLE ai_messages IS 'AI conversation messages';
COMMENT ON TABLE user_preferences IS 'User course preferences';

COMMENT ON COLUMN ai_conversations.conversation_id IS 'Frontend generated unique conversation ID';
COMMENT ON COLUMN ai_conversations.context IS 'Conversation context JSON: {courseType, credits, keywords, difficulty, faculty}';
COMMENT ON COLUMN ai_messages.role IS 'Message role: user or ai';
COMMENT ON COLUMN ai_messages.message_type IS 'Message type: text or recommendation';
COMMENT ON COLUMN ai_messages.courses IS 'Recommended courses JSON array with details and reasons';

