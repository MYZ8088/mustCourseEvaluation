package com.must.courseevaluation.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContentFilterService {
    
    // 敏感词列表
    private final List<String> sensitiveWords = Arrays.asList(
        "傻逼", "操你", "他妈的", "妈的", "狗日", "混蛋", "王八蛋", 
        "fuck", "shit", "damn", "asshole", "bitch", 
        "废物", "垃圾", "sb", "tmb", "gun"
    );
    
    // 检查评论是否包含敏感词
    public boolean containsSensitiveContent(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        return sensitiveWords.stream()
                .anyMatch(lowerContent::contains);
    }
    
    // 过滤敏感内容
    public String filterContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        String filteredContent = content;
        for (String word : sensitiveWords) {
            // 创建与敏感词长度相同的星号字符串
            String replacement = "*".repeat(word.length());
            // 替换敏感词（忽略大小写）
            filteredContent = Pattern.compile(word, Pattern.CASE_INSENSITIVE)
                    .matcher(filteredContent)
                    .replaceAll(replacement);
        }
        
        return filteredContent;
    }
} 