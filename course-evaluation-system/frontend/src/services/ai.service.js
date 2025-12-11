import axios from 'axios';

/**
 * AI服务 - 封装DeepSeek API调用（使用OpenAI格式）
 */
class AIService {
  constructor() {
    this.apiKey = process.env.VUE_APP_DEEPSEEK_API_KEY || '';
    this.baseURL = process.env.VUE_APP_DEEPSEEK_BASE_URL || 'https://api.deepseek.com';
    this.enabled = process.env.VUE_APP_AI_ENABLED === 'true';
    this.model = 'deepseek-chat';

    // 创建axios实例
    this.client = axios.create({
      baseURL: `${this.baseURL}/v1`,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.apiKey}`
      },
      timeout: 30000
    });
  }

  /**
   * 检查AI服务是否可用
   */
  isAvailable() {
    return this.enabled && this.apiKey && this.apiKey.length > 0;
  }

  /**
   * 第一层：意图解析
   * 将用户自然语言转换为结构化参数
   */
  async parseIntent(userMessage, conversationContext = {}) {
    if (!this.isAvailable()) {
      throw new Error('AI服务未配置或未启用');
    }

    const systemPrompt = this._buildIntentParserPrompt();
    const userPrompt = this._buildUserContextPrompt(userMessage, conversationContext);

    try {
      const response = await this.client.post('/chat/completions', {
        model: this.model,
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: userPrompt }
        ],
        temperature: 0.3, // 降低温度以获得更稳定的结构化输出
        max_tokens: 500,
        response_format: { type: 'json_object' } // 强制JSON输出
      });

      const content = response.data.choices[0].message.content;
      const result = JSON.parse(content);

      return {
        intent: result.intent || 'query',
        parameters: {
          courseType: result.parameters?.courseType || conversationContext.courseType || null,
          credits: result.parameters?.credits || conversationContext.credits || null,
          keywords: result.parameters?.keywords || conversationContext.keywords || [],
          difficulty: result.parameters?.difficulty || conversationContext.difficulty || null,
          faculty: result.parameters?.faculty || conversationContext.faculty || null,
          teacher: result.parameters?.teacher || conversationContext.teacher || null
        },
        confidence: result.confidence || 0.8,
        needMoreInfo: result.needMoreInfo || false,
        nextQuestion: result.nextQuestion || null
      };
    } catch (error) {
      console.error('AI意图解析失败:', error);
      throw new Error('AI服务暂时不可用，请稍后再试');
    }
  }

  /**
   * 第三层：话术生成
   * 为推荐结果生成自然、友好的介绍文案
   */
  async generateResponse(userParameters, recommendedCourses) {
    if (!this.isAvailable()) {
      throw new Error('AI服务未配置或未启用');
    }

    const systemPrompt = this._buildResponseGeneratorPrompt();
    const userPrompt = this._buildRecommendationPrompt(userParameters, recommendedCourses);

    try {
      const response = await this.client.post('/chat/completions', {
        model: this.model,
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: userPrompt }
        ],
        temperature: 0.7, // 稍高温度以获得更自然的文本
        max_tokens: 800,
        response_format: { type: 'json_object' }
      });

      const content = response.data.choices[0].message.content;
      const result = JSON.parse(content);

      return {
        greeting: result.greeting || '根据您的需求，我为您推荐以下课程：',
        courses: result.courses || recommendedCourses.map(c => ({
          course_id: c.id,
          reason: '这门课程符合您的需求'
        })),
        suggestion: result.suggestion || ''
      };
    } catch (error) {
      console.error('AI话术生成失败:', error);
      throw new Error('AI服务暂时不可用，请稍后再试');
    }
  }

  /**
   * 构建意图解析的System Prompt
   */
  _buildIntentParserPrompt() {
    return `你是一个专业的课程需求分析专家。你的任务是从用户的自然语言消息中提取结构化的课程需求参数。

## 可用的学院列表（你需要将用户的模糊表达智能映射到具体学院）：
- 创新工程学院：包含计算机、软件、编程、IT、人工智能、AI、算法、数据结构、数据库等相关课程
- 商学院：包含经济、金融、会计、管理、营销、投资等相关课程
- 人文艺术学院：包含艺术、设计、文化、写作、媒体、创意等相关课程
- 酒店与旅游管理学院：包含酒店、旅游、会展、餐饮、服务等相关课程
- 医学院：包含医学、医疗、临床、药理、解剖、生理、健康等相关课程

## 可用的教师列表：
- 陈伟（创新工程学院，教授）：人工智能与机器学习专家
- 林晓明（创新工程学院，副教授）：软件工程与系统架构专家
- 黄建华（商学院，教授）：财务管理与投资分析专家
- 周梅（商学院，副教授）：市场营销策略专家
- 王艺琳（人文艺术学院，教授）：设计与艺术评论家
- 刘芳（人文艺术学院，副教授）：文化研究与创意写作专家
- 张红（酒店与旅游管理学院，教授）：酒店管理专家
- 李强（酒店与旅游管理学院，副教授）：旅游经济学专家
- 赵明德（医学院，教授）：内科主任医师
- 孙丽丽（医学院，副教授）：临床药理学专家

## 可提取的参数：
- courseType: 课程类型，只能是"COMPULSORY"（必修课）或"ELECTIVE"（选修课）
- credits: 学分数字（如2、3、4等）
- keywords: 兴趣关键词数组（如：["编程", "数学", "设计"]等）
- difficulty: 难度，只能是"easy"（简单）、"medium"（中等）或"hard"（困难）
- faculty: 学院名称（必须是上述5个学院之一的完整名称）
- teacher: 教师姓名（必须是上述教师之一）

## 智能映射规则：
1. 当用户提到"编程"、"计算机"、"软件"、"AI"、"人工智能"等词时，映射到 faculty: "创新工程学院"
2. 当用户提到"医学"、"医疗"、"临床"、"药"、"健康"等词时，映射到 faculty: "医学院"
3. 当用户提到"经济"、"金融"、"会计"、"商业"、"营销"等词时，映射到 faculty: "商学院"
4. 当用户提到"设计"、"艺术"、"写作"、"媒体"等词时，映射到 faculty: "人文艺术学院"
5. 当用户提到"酒店"、"旅游"、"会展"等词时，映射到 faculty: "酒店与旅游管理学院"
6. 当用户提到具体教师名字时，提取到 teacher 参数

## 规则：
1. 优先进行学院和教师的智能映射，将用户的模糊表达转换为明确参数
2. 没有明确提到的参数设为null或空数组
3. 当有明确的学院或教师需求时，设置 needMoreInfo 为 false，直接进行推荐
4. 只有在用户需求非常模糊时才设置 needMoreInfo 为 true

必须严格按照以下JSON格式输出：
{
  "intent": "query",
  "parameters": {
    "courseType": null,
    "credits": null,
    "keywords": [],
    "difficulty": null,
    "faculty": null,
    "teacher": null
  },
  "confidence": 0.9,
  "needMoreInfo": false,
  "nextQuestion": null
}`;
  }

  /**
   * 构建话术生成的System Prompt
   */
  _buildResponseGeneratorPrompt() {
    return `你是一个友好、专业的课程推荐顾问。你的任务是为推荐的课程生成自然、个性化的介绍文案。

要求：
1. 语气要友好、热情，但不过分夸张
2. 为每门课程生成独特的推荐理由（基于课程特点，如评分、难度、实用性等）
3. 推荐理由要具体、有说服力
4. 可以适当提供学习建议

必须严格按照以下JSON格式输出：
{
  "greeting": "根据您的需求，我为您精选了以下课程：",
  "courses": [
    {
      "course_id": 1,
      "reason": "这门课程评分高达4.5分，内容循序渐进，非常适合初学者入门"
    }
  ],
  "suggestion": "建议您优先考虑第一门课程，它的难度适中且实用性强。"
}`;
  }

  /**
   * 构建用户上下文Prompt
   */
  _buildUserContextPrompt(userMessage, context) {
    let prompt = `用户消息：${userMessage}\n\n`;

    if (Object.keys(context).length > 0) {
      prompt += '已知的用户需求：\n';
      if (context.courseType) {
        prompt += `- 课程类型：${context.courseType === 'COMPULSORY' ? '必修课' : '选修课'}\n`;
      }
      if (context.credits) {
        prompt += `- 学分：${context.credits}\n`;
      }
      if (context.keywords && context.keywords.length > 0) {
        prompt += `- 兴趣关键词：${context.keywords.join('、')}\n`;
      }
      if (context.difficulty) {
        prompt += `- 难度偏好：${context.difficulty}\n`;
      }
      if (context.faculty) {
        prompt += `- 学院：${context.faculty}\n`;
      }
      if (context.teacher) {
        prompt += `- 教师：${context.teacher}\n`;
      }
      prompt += '\n';
    }

    prompt += '请分析用户消息，提取新的需求参数（特别注意将用户的模糊表达映射到具体学院），并判断是否需要询问更多信息。';

    return prompt;
  }

  /**
   * 构建推荐课程的Prompt
   */
  _buildRecommendationPrompt(parameters, courses) {
    let prompt = '用户需求：\n';

    if (parameters.courseType) {
      prompt += `- 课程类型：${parameters.courseType === 'COMPULSORY' ? '必修课' : '选修课'}\n`;
    }
    if (parameters.credits) {
      prompt += `- 学分：${parameters.credits}\n`;
    }
    if (parameters.keywords && parameters.keywords.length > 0) {
      prompt += `- 兴趣关键词：${parameters.keywords.join('、')}\n`;
    }
    if (parameters.difficulty) {
      const difficultyMap = { easy: '简单', medium: '中等', hard: '困难' };
      prompt += `- 难度偏好：${difficultyMap[parameters.difficulty] || parameters.difficulty}\n`;
    }

    prompt += '\n推荐的课程列表：\n';
    courses.forEach((course, index) => {
      prompt += `${index + 1}. ${course.name} (${course.code})\n`;
      prompt += `   - 课程ID: ${course.id}\n`;
      prompt += `   - 学分: ${course.credits}\n`;
      prompt += `   - 类型: ${course.type === 'COMPULSORY' ? '必修' : '选修'}\n`;
      if (course.averageRating) {
        prompt += `   - 评分: ${course.averageRating.toFixed(1)}/5.0\n`;
      }
      if (course.reviewCount) {
        prompt += `   - 评价数: ${course.reviewCount}条\n`;
      }
      if (course.teacherName) {
        prompt += `   - 授课教师: ${course.teacherName}\n`;
      }
      if (course.description) {
        prompt += `   - 简介: ${course.description.substring(0, 100)}...\n`;
      }
      prompt += '\n';
    });

    prompt += '请为这些课程生成友好的介绍文案和个性化推荐理由。';

    return prompt;
  }

  /**
   * 获取课程AI总结（优先返回缓存，如无缓存且评论>=10则自动生成）
   * @param {Number} courseId 课程ID
   * @returns {Object} 包含 available, reviewCount, summary 或 message
   */
  async getCourseSummary(courseId) {
    try {
      // GET 请求获取缓存或自动生成
      const response = await axios.get(`/api/courses/${courseId}/ai-summary`);
      return response.data;
    } catch (error) {
      console.error('获取AI总结失败:', error);
      if (error.response && error.response.data && error.response.data.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error('获取AI总结失败，请稍后再试');
    }
  }

  /**
   * 强制重新生成课程AI总结（管理员专用）
   * @param {Number} courseId 课程ID
   * @returns {Object} 包含 available, reviewCount, summary
   */
  async regenerateCourseSummary(courseId) {
    try {
      // POST 请求强制重新生成
      const response = await axios.post(`/api/courses/${courseId}/ai-summary`);
      return response.data;
    } catch (error) {
      console.error('重新生成AI总结失败:', error);
      if (error.response && error.response.status === 403) {
        throw new Error('只有管理员可以重新生成AI总结');
      }
      if (error.response && error.response.data && error.response.data.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error('AI服务暂时不可用，请稍后再试');
    }
  }

  /**
   * 检查后端AI服务状态
   */
  async checkBackendAIStatus() {
    try {
      const response = await axios.get('/api/courses/ai-status');
      return response.data;
    } catch (error) {
      console.error('检查AI服务状态失败:', error);
      return { available: false, message: '无法连接到服务器' };
    }
  }

  /**
   * 测试API连接
   */
  async testConnection() {
    if (!this.isAvailable()) {
      return { success: false, message: 'AI服务未配置' };
    }

    try {
      const response = await this.client.post('/chat/completions', {
        model: this.model,
        messages: [
          { role: 'user', content: '你好' }
        ],
        max_tokens: 10
      });

      return {
        success: true,
        message: 'AI服务连接成功',
        model: this.model
      };
    } catch (error) {
      return {
        success: false,
        message: `AI服务连接失败: ${error.message}`
      };
    }
  }
}

export default new AIService();


