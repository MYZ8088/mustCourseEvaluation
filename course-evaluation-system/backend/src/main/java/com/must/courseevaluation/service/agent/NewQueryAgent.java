package com.must.courseevaluation.service.agent;

import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.CourseSchedule;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.CourseScheduleRepository;
import com.must.courseevaluation.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 新查询 Agent
 * 处理全新的课程推荐请求，从全部课程中推荐
 */
@Component
public class NewQueryAgent implements BaseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(NewQueryAgent.class);
    
    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final ReviewRepository reviewRepository;
    
    public NewQueryAgent(CourseRepository courseRepository, 
                         CourseScheduleRepository courseScheduleRepository,
                         ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.courseScheduleRepository = courseScheduleRepository;
        this.reviewRepository = reviewRepository;
    }
    
    @Override
    public IntentType getSupportedIntentType() {
        return IntentType.NEW_QUERY;
    }
    
    @Override
    public AgentResult process(IntentResult intent, ConversationContext context) {
        logger.info("[NewQueryAgent] 处理新查询: {}", intent.getOriginalMessage());
        
        // 获取所有课程
        List<Course> allCourses = courseRepository.findAll();
        
        // 合并参数
        Map<String, Object> params = new HashMap<>(context.getParameters());
        params.putAll(intent.getParameters());
        
        // 过滤课程
        List<Course> filteredCourses = filterCourses(allCourses, params, intent.getKeywords());
        logger.info("[NewQueryAgent] 过滤后课程数量: {}", filteredCourses.size());
        
        if (filteredCourses.isEmpty()) {
            return AgentResult.text(IntentType.NEW_QUERY, 
                "抱歉，根据您的需求，我暂时没有找到完全匹配的课程。您可以调整一下条件，比如更改学院或课程类型，我会继续为您查找。");
        }
        
        // 限制数量
        List<Course> recommendedCourses = filteredCourses.stream()
                .limit(5)
                .collect(Collectors.toList());
        
        // 转换为 CourseInfo
        List<AgentResult.CourseInfo> courseInfos = recommendedCourses.stream()
                .map(this::toCourseInfo)
                .collect(Collectors.toList());
        
        // 更新上下文
        Map<String, Object> updatedContext = new HashMap<>(params);
        updatedContext.put("lastIntentType", IntentType.NEW_QUERY.name());
        
        return AgentResult.builder()
                .intentType(IntentType.NEW_QUERY)
                .success(true)
                .courses(courseInfos)
                .updatedContext(updatedContext)
                .build();
    }
    
    private List<Course> filterCourses(List<Course> courses, Map<String, Object> params, List<String> keywords) {
        // 检查是否有结构化参数（学院、教师、课程类型、学分、时间）
        boolean hasStructuredParams = params.containsKey("faculty") || 
                                      params.containsKey("teacher") || 
                                      params.containsKey("courseType") || 
                                      params.containsKey("credits") ||
                                      params.containsKey("dayOfWeek") ||
                                      params.containsKey("timePeriod");
        
        // 预先获取符合时间条件的课程ID集合
        Set<Long> timeMatchedCourseIds = getTimeMatchedCourseIds(params);
        
        List<Course> filteredByParams = courses.stream()
                .filter(course -> {
                    // 时间过滤（星期、时间段）
                    if (timeMatchedCourseIds != null && !timeMatchedCourseIds.contains(course.getId())) {
                        return false;
                    }
                    
                    // 学院过滤
                    if (params.containsKey("faculty")) {
                        String faculty = (String) params.get("faculty");
                        String courseFaculty = course.getFaculty() != null ? course.getFaculty().getName() : "";
                        if (!courseFaculty.contains(faculty) && !faculty.contains(courseFaculty)) {
                            return false;
                        }
                    }
                    
                    // 教师过滤
                    if (params.containsKey("teacher")) {
                        String teacher = (String) params.get("teacher");
                        String courseTeacher = course.getTeacher() != null ? course.getTeacher().getName() : "";
                        if (!courseTeacher.contains(teacher) && !teacher.contains(courseTeacher)) {
                            return false;
                        }
                    }
                    
                    // 课程类型过滤
                    if (params.containsKey("courseType")) {
                        String courseType = (String) params.get("courseType");
                        if (!course.getType().name().equals(courseType)) {
                            return false;
                        }
                    }
                    
                    // 学分过滤
                    if (params.containsKey("credits")) {
                        Object creditsObj = params.get("credits");
                        int credits;
                        if (creditsObj instanceof Integer) {
                            credits = (Integer) creditsObj;
                        } else if (creditsObj instanceof Number) {
                            credits = ((Number) creditsObj).intValue();
                        } else {
                            return true; // 无法解析学分，跳过此过滤
                        }
                        double diff = Math.abs(course.getCredits() - credits);
                        if (diff > 0.5) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 如果有结构化参数且过滤后有结果，直接返回（不强制关键词匹配）
        // 这样"创业课程"能通过学院映射找到商学院的课程
        if (hasStructuredParams && !filteredByParams.isEmpty()) {
            logger.info("[NewQueryAgent] 使用结构化参数过滤，找到 {} 门课程", filteredByParams.size());
            return filteredByParams.stream()
                    .sorted((a, b) -> {
                        // 如果有关键词，优先返回匹配关键词的课程
                        if (keywords != null && !keywords.isEmpty()) {
                            boolean aMatch = matchKeywords(a, keywords);
                            boolean bMatch = matchKeywords(b, keywords);
                            if (aMatch && !bMatch) return -1;
                            if (!aMatch && bMatch) return 1;
                        }
                        // 其次按评分排序
                        Double ratingA = getAverageRating(a);
                        Double ratingB = getAverageRating(b);
                        ratingA = ratingA != null ? ratingA : 0.0;
                        ratingB = ratingB != null ? ratingB : 0.0;
                        return ratingB.compareTo(ratingA);
                    })
                    .collect(Collectors.toList());
        }
        
        // 如果没有结构化参数，或结构化参数过滤后没有结果，则使用关键词匹配
        if (keywords != null && !keywords.isEmpty()) {
            List<Course> keywordMatched = courses.stream()
                    .filter(course -> matchKeywords(course, keywords))
                    .sorted((a, b) -> {
                        Double ratingA = getAverageRating(a);
                        Double ratingB = getAverageRating(b);
                        ratingA = ratingA != null ? ratingA : 0.0;
                        ratingB = ratingB != null ? ratingB : 0.0;
                        return ratingB.compareTo(ratingA);
                    })
                    .collect(Collectors.toList());
            
            if (!keywordMatched.isEmpty()) {
                logger.info("[NewQueryAgent] 使用关键词匹配，找到 {} 门课程", keywordMatched.size());
                return keywordMatched;
            }
        }
        
        // 最后返回参数过滤结果（可能为空）
        return filteredByParams.stream()
                .sorted((a, b) -> {
                    Double ratingA = getAverageRating(a);
                    Double ratingB = getAverageRating(b);
                    ratingA = ratingA != null ? ratingA : 0.0;
                    ratingB = ratingB != null ? ratingB : 0.0;
                    return ratingB.compareTo(ratingA);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据时间参数获取匹配的课程ID集合
     * @return 匹配的课程ID集合，如果没有时间参数则返回null（表示不过滤）
     */
    private Set<Long> getTimeMatchedCourseIds(Map<String, Object> params) {
        List<Integer> dayOfWeekList = null;
        Integer timePeriod = null;
        
        // dayOfWeek 支持 List 或单个数值
        if (params.containsKey("dayOfWeek")) {
            Object obj = params.get("dayOfWeek");
            if (obj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> days = (List<Object>) obj;
                dayOfWeekList = new ArrayList<>();
                for (Object d : days) {
                    if (d instanceof Number) {
                        dayOfWeekList.add(((Number) d).intValue());
                    }
                }
            } else if (obj instanceof Number) {
                dayOfWeekList = List.of(((Number) obj).intValue());
            }
        }
        
        if (params.containsKey("timePeriod")) {
            Object obj = params.get("timePeriod");
            if (obj instanceof Integer) {
                timePeriod = (Integer) obj;
            } else if (obj instanceof Number) {
                timePeriod = ((Number) obj).intValue();
            }
        }
        
        // 如果没有时间参数，返回null表示不过滤
        if ((dayOfWeekList == null || dayOfWeekList.isEmpty()) && timePeriod == null) {
            return null;
        }
        
        Set<Long> matchedCourseIds = new HashSet<>();
        
        if (dayOfWeekList != null && !dayOfWeekList.isEmpty()) {
            // 查询匹配任意一个星期的课程
            for (Integer dayOfWeek : dayOfWeekList) {
                List<CourseSchedule> schedules;
                if (timePeriod != null) {
                    schedules = courseScheduleRepository.findByDayOfWeekAndTimePeriod(dayOfWeek, timePeriod);
                    logger.info("[NewQueryAgent] 时间过滤: 周{} 时间段{}, 找到 {} 个时间安排", 
                            dayOfWeek, timePeriod, schedules.size());
                } else {
                    schedules = courseScheduleRepository.findByDayOfWeek(dayOfWeek);
                    logger.info("[NewQueryAgent] 时间过滤: 周{}, 找到 {} 个时间安排", dayOfWeek, schedules.size());
                }
                schedules.forEach(s -> matchedCourseIds.add(s.getCourse().getId()));
            }
        } else if (timePeriod != null) {
            // 只指定时间段
            List<CourseSchedule> schedules = courseScheduleRepository.findByTimePeriod(timePeriod);
            logger.info("[NewQueryAgent] 时间过滤: 时间段{}, 找到 {} 个时间安排", timePeriod, schedules.size());
            schedules.forEach(s -> matchedCourseIds.add(s.getCourse().getId()));
        }
        
        logger.info("[NewQueryAgent] 时间过滤后匹配的课程ID数量: {}", matchedCourseIds.size());
        return matchedCourseIds;
    }
    
    /**
     * 检查课程是否匹配关键词
     */
    private boolean matchKeywords(Course course, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        String searchText = (course.getName() + " " + 
                            (course.getDescription() != null ? course.getDescription() : "")).toLowerCase();
        for (String keyword : keywords) {
            if (searchText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private AgentResult.CourseInfo toCourseInfo(Course course) {
        return AgentResult.CourseInfo.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .type(course.getType().name())
                .description(course.getDescription())
                .facultyName(course.getFaculty() != null ? course.getFaculty().getName() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .averageRating(getAverageRating(course))
                .reviewCount(getReviewCount(course))
                .build();
    }
    
    private Double getAverageRating(Course course) {
        return reviewRepository.getAverageRatingForCourse(course);
    }
    
    private Integer getReviewCount(Course course) {
        Long count = reviewRepository.getReviewCountForCourse(course);
        return count != null ? count.intValue() : 0;
    }
}

