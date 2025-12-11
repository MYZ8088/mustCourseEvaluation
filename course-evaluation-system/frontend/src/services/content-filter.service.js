// 前端内容过滤服务
class ContentFilterService {
  constructor() {
    // 敏感词列表
    this.sensitiveWords = [
      "傻逼", "操你", "他妈的", "妈的", "狗日", "混蛋", "王八蛋", 
      "fuck", "shit", "damn", "asshole", "bitch", 
      "废物", "垃圾", "sb", "tmb", "gun"
    ];
  }
  
  // 检查内容是否包含敏感词
  containsSensitiveContent(content) {
    if (!content || content.trim() === '') {
      return false;
    }
    
    const lowerContent = content.toLowerCase();
    return this.sensitiveWords.some(word => lowerContent.includes(word));
  }
  
  // 获取内容中的敏感词
  getSensitiveWordsInContent(content) {
    if (!content || content.trim() === '') {
      return [];
    }
    
    const lowerContent = content.toLowerCase();
    return this.sensitiveWords.filter(word => lowerContent.includes(word));
  }
  
  // 过滤敏感内容
  filterContent(content) {
    if (!content || content.trim() === '') {
      return content;
    }
    
    let filteredContent = content;
    this.sensitiveWords.forEach(word => {
      // 创建正则表达式，忽略大小写
      const regex = new RegExp(word, 'gi');
      // 替换为星号
      filteredContent = filteredContent.replace(regex, '*'.repeat(word.length));
    });
    
    return filteredContent;
  }
}

export default new ContentFilterService(); 