package com.must.courseevaluation.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 意图分类 Agent
 * 负责识别用户意图类型，提取关键参数
 */
@Component
public class IntentClassifierAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(IntentClassifierAgent.class);
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 对用户消息进行意图分类
     */
    public IntentResult classify(String message, ConversationContext context) {
        try {
            String systemPrompt = buildClassifierPrompt();
            String userPrompt = buildUserPrompt(message, context);
            
            String response = callDeepSeekAPI(systemPrompt, userPrompt);
            return parseClassificationResult(response, message);
            
        } catch (Exception e) {
            logger.error("意图分类失败: {}", e.getMessage(), e);
            // 降级：返回新查询类型
            return IntentResult.builder()
                    .intentType(IntentType.NEW_QUERY)
                    .originalMessage(message)
                    .confidence(0.5)
                    .build();
        }
    }
    
    private String buildClassifierPrompt() {
        return """
            你是一个智能意图分类器，负责分析用户在课程推荐对话中的意图，并提取相关参数。
            
            ## 意图类型定义
            
            1. **NEW_QUERY** - 全新的课程推荐请求
               - 用户开始一个新的课程查询
               - 示例："推荐编程课程"、"有什么医学课程"、"我想学习创业"、"周三上午有什么课"
            
            2. **REFINE** - 从上次推荐中筛选/细化
               - 用户在之前推荐的课程基础上进一步筛选
               - 关键词：上述、刚才、这些、其中、里面、推荐的、上面
               - 示例："上述课程中有神经网络的吗"、"这些课程哪个更简单"、"刚才推荐的有选修课吗"
            
            3. **SUPPLEMENT** - 补充/修改条件
               - 用户补充或修改之前的搜索条件，需要重新推荐
               - 示例："要选修课"、"3学分的"、"简单点的"、"换成商学院的"、"改成晚上的课"
            
            4. **COMPARE** - 比较课程
               - 用户想比较两门或多门课程
               - 示例："这两门课哪个更好"、"数据库和算法课有什么区别"
            
            5. **DETAIL** - 询问课程详情
               - 用户询问特定课程的详细信息
               - 示例："人工智能导论讲什么"、"这门课难吗"、"XXX课程怎么样"
            
            6. **CHAT** - 闲聊/其他
               - 非课程推荐相关的对话
               - 示例："谢谢"、"你好"、"再见"、"好的"
            
            ## 分类规则
            
            1. 如果用户使用"上述"、"刚才"、"这些"、"其中"等词引用之前的推荐，且有上次推荐记录，分类为 REFINE
            2. 如果用户只是补充条件（如"要选修课"），且是对之前查询的补充，分类为 SUPPLEMENT
            3. 如果用户明确提出新的领域/方向（如"推荐医学课程"），分类为 NEW_QUERY
            4. 简短的肯定/感谢/问候语分类为 CHAT
            
            ## 可映射的参数选项
            
            请根据用户消息智能判断是否能映射到以下参数。只有当用户消息明确相关时才提取，不相关则设为 null。
            
            ### 学院 (faculty) - 根据用户提到的领域智能映射
            - "创新工程学院"：计算机、软件、编程、代码、人工智能、AI、机器学习、算法、数据库、网络、系统等
            - "商学院"：经济、金融、会计、财务、营销、市场、投资、创业、商业、管理、贸易等
            - "人文艺术学院"：设计、艺术、绘画、写作、文学、媒体、传播、文化、创意等
            - "酒店与旅游管理学院"：酒店、旅游、会展、餐饮、服务业、款待等
            - "医学院"：医学、医疗、健康、药物、药理、解剖、生理、临床、护理等
            
            ### 课程类型 (courseType)
            - "COMPULSORY"：必修课、必修、核心课
            - "ELECTIVE"：选修课、选修、公选
            
            ### 学分 (credits)
            - 数字（2、3、4等），用户提到"X学分"时提取
            
            ### 星期 (dayOfWeek) - 用户提到上课日期时提取，支持数组
            - 可以是单个值或数组，如 1 或 [1, 3]
            - 1 = 周一、星期一、礼拜一
            - 2 = 周二、星期二、礼拜二
            - 3 = 周三、星期三、礼拜三
            - 4 = 周四、星期四、礼拜四
            - 5 = 周五、星期五、礼拜五
            - 6 = 周六、星期六、礼拜六
            - 7 = 周日、星期日、礼拜日
            - 用户说"周一周三"应提取为 [1, 3]，说"周末"应提取为 [6, 7]
            
            ### 上课时间段 (timePeriod) - 用户提到上课时间时提取
            - 1 = 上午、早上、morning（09:00-11:50）
            - 2 = 中午、午间（12:30-15:20）
            - 3 = 下午、afternoon（15:30-18:20）
            - 4 = 晚上、夜间、evening、晚课（19:00-21:50）
            
            ### 教师 (teacher)
            - 教师姓名，用户提到"XXX老师"、"XXX教授"时提取
            
            ### 难度 (difficulty)
            - "easy"：简单、容易、轻松
            - "medium"：适中、一般
            - "hard"：难、困难、有挑战
            
            ## 输出格式
            
            必须严格按照以下JSON格式输出：
            {
                "intentType": "NEW_QUERY|REFINE|SUPPLEMENT|COMPARE|DETAIL|CHAT",
                "confidence": 0.95,
                "referenceLastResult": false,
                "keywords": ["关键词1", "关键词2"],
                "parameters": {
                    "faculty": null,
                    "teacher": null,
                    "courseType": null,
                    "credits": null,
                    "difficulty": null,
                    "dayOfWeek": null 或 [1, 3] (支持单值或数组),
                    "timePeriod": null
                },
                "coursesToCompare": [],
                "courseToQuery": null,
                "reasoning": "分类理由"
            }
            
            ## 重要提示
            
            1. 参数只有在用户消息中明确相关时才提取，否则设为 null
            2. 可以同时提取多个参数，如"周三上午的编程课"应提取 dayOfWeek=3, timePeriod=1, faculty="创新工程学院"
            3. keywords 用于提取用户消息中的关键搜索词，帮助后续课程匹配
            """;
    }
    
    private String buildUserPrompt(String message, ConversationContext context) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加对话历史（最近5条）
        if (context.getHistory() != null && !context.getHistory().isEmpty()) {
            prompt.append("=== 对话历史 ===\n");
            int start = Math.max(0, context.getHistory().size() - 5);
            for (int i = start; i < context.getHistory().size(); i++) {
                var msg = context.getHistory().get(i);
                prompt.append(msg.getRole().equals("user") ? "用户: " : "助手: ");
                String content = msg.getContent();
                if (content != null && content.length() > 80) {
                    content = content.substring(0, 80) + "...";
                }
                prompt.append(content).append("\n");
            }
            prompt.append("\n");
        }
        
        // 添加上次推荐的课程（重要！）
        if (context.hasLastRecommendedCourses()) {
            prompt.append("=== 上次推荐的课程 ===\n");
            for (AgentResult.CourseInfo course : context.getLastRecommendedCourses()) {
                prompt.append("- ").append(course.getName());
                if (course.getFacultyName() != null) {
                    prompt.append(" (").append(course.getFacultyName()).append(")");
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // 添加当前已知参数（非常重要！用于上下文记忆）
        if (context.getParameters() != null && !context.getParameters().isEmpty()) {
            prompt.append("=== 当前已知参数（用户之前的筛选条件）===\n");
            Map<String, Object> params = context.getParameters();
            if (params.containsKey("faculty")) prompt.append("- 学院: ").append(params.get("faculty")).append("\n");
            if (params.containsKey("courseType")) prompt.append("- 类型: ").append(params.get("courseType")).append("\n");
            if (params.containsKey("credits")) prompt.append("- 学分: ").append(params.get("credits")).append("\n");
            if (params.containsKey("dayOfWeek")) {
                String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                Object dayOfWeekObj = params.get("dayOfWeek");
                if (dayOfWeekObj instanceof List) {
                    // List 形式
                    @SuppressWarnings("unchecked")
                    List<Object> days = (List<Object>) dayOfWeekObj;
                    StringBuilder dayStr = new StringBuilder();
                    for (Object d : days) {
                        int day = ((Number) d).intValue();
                        if (day >= 1 && day <= 7) {
                            if (dayStr.length() > 0) dayStr.append("、");
                            dayStr.append(dayNames[day]);
                        }
                    }
                    if (dayStr.length() > 0) {
                        prompt.append("- 星期: ").append(dayStr).append("\n");
                    }
                } else if (dayOfWeekObj instanceof Number) {
                    // 单值形式
                    int day = ((Number) dayOfWeekObj).intValue();
                    if (day >= 1 && day <= 7) {
                        prompt.append("- 星期: ").append(dayNames[day]).append("\n");
                    }
                }
            }
            if (params.containsKey("timePeriod")) {
                // 处理 Integer 或 Long 类型
                int period = ((Number) params.get("timePeriod")).intValue();
                String[] periodNames = {"", "上午(09:00-11:50)", "中午(12:30-15:20)", "下午(15:30-18:20)", "晚上(19:00-21:50)"};
                if (period >= 1 && period <= 4) {
                    prompt.append("- 时间段: ").append(periodNames[period]).append("\n");
                }
            }
            if (params.containsKey("teacher")) prompt.append("- 教师: ").append(params.get("teacher")).append("\n");
            prompt.append("注意：用户新的查询可能是在这些条件基础上进一步筛选！\n\n");
        }
        
        prompt.append("=== 当前用户消息 ===\n");
        prompt.append(message).append("\n\n");
        prompt.append("请分析用户意图并输出JSON结果。");
        
        return prompt.toString();
    }
    
    private IntentResult parseClassificationResult(String response, String originalMessage) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        
        IntentResult result = new IntentResult();
        result.setOriginalMessage(originalMessage);
        
        // 解析意图类型
        String intentTypeStr = root.path("intentType").asText("NEW_QUERY");
        try {
            result.setIntentType(IntentType.valueOf(intentTypeStr));
        } catch (Exception e) {
            result.setIntentType(IntentType.NEW_QUERY);
        }
        
        // 解析置信度
        result.setConfidence(root.path("confidence").asDouble(0.8));
        
        // 解析是否引用上次结果
        result.setReferenceLastResult(root.path("referenceLastResult").asBoolean(false));
        
        // 解析关键词
        List<String> keywords = new ArrayList<>();
        root.path("keywords").forEach(node -> keywords.add(node.asText()));
        result.setKeywords(keywords);
        
        // 解析参数
        Map<String, Object> parameters = new HashMap<>();
        JsonNode params = root.path("parameters");
        if (!params.path("faculty").isNull() && !params.path("faculty").asText().isEmpty()) {
            parameters.put("faculty", params.path("faculty").asText());
        }
        if (!params.path("teacher").isNull() && !params.path("teacher").asText().isEmpty()) {
            parameters.put("teacher", params.path("teacher").asText());
        }
        if (!params.path("courseType").isNull() && !params.path("courseType").asText().isEmpty()) {
            parameters.put("courseType", params.path("courseType").asText());
        }
        if (!params.path("credits").isNull() && params.path("credits").isNumber()) {
            parameters.put("credits", params.path("credits").asInt());
        }
        if (!params.path("difficulty").isNull() && !params.path("difficulty").asText().isEmpty()) {
            parameters.put("difficulty", params.path("difficulty").asText());
        }
        // 解析星期 (dayOfWeek) - 支持单值或数组
        if (!params.path("dayOfWeek").isNull()) {
            JsonNode dayOfWeekNode = params.path("dayOfWeek");
            if (dayOfWeekNode.isArray()) {
                // 数组形式: [1, 3]
                List<Integer> days = new ArrayList<>();
                for (JsonNode day : dayOfWeekNode) {
                    int d = day.asInt();
                    if (d >= 1 && d <= 7) {
                        days.add(d);
                    }
                }
                if (!days.isEmpty()) {
                    parameters.put("dayOfWeek", days);
                }
            } else if (dayOfWeekNode.isNumber()) {
                // 单值形式: 1
                int dayOfWeek = dayOfWeekNode.asInt();
                if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                    // 统一存储为 List 以简化后续处理
                    parameters.put("dayOfWeek", List.of(dayOfWeek));
                }
            }
        }
        // 解析时间段 (timePeriod)
        if (!params.path("timePeriod").isNull() && params.path("timePeriod").isNumber()) {
            int timePeriod = params.path("timePeriod").asInt();
            if (timePeriod >= 1 && timePeriod <= 4) {
                parameters.put("timePeriod", timePeriod);
            }
        }
        result.setParameters(parameters);
        
        logger.info("[意图分类] 参数: {}", parameters);
        
        // 解析比较课程
        List<String> coursesToCompare = new ArrayList<>();
        root.path("coursesToCompare").forEach(node -> coursesToCompare.add(node.asText()));
        result.setCoursesToCompare(coursesToCompare);
        
        // 解析查询课程
        if (!root.path("courseToQuery").isNull()) {
            result.setCourseToQuery(root.path("courseToQuery").asText());
        }
        
        logger.info("[意图分类] 类型: {}, 置信度: {}, 引用上次: {}", 
                result.getIntentType(), result.getConfidence(), result.isReferenceLastResult());
        
        return result;
    }
    
    private String callDeepSeekAPI(String systemPrompt, String userPrompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);
        requestBody.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = apiUrl + "/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        }

        throw new RuntimeException("DeepSeek API 调用失败");
    }
}

