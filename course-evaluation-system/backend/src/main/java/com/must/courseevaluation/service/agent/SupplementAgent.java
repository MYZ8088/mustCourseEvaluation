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
 * 条件补充 Agent
 * 处理用户补充/修改条件的情况，更新参数后重新推荐
 */
@Component
public class SupplementAgent implements BaseAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplementAgent.class);
    
    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final ReviewRepository reviewRepository;
    
    public SupplementAgent(CourseRepository courseRepository, 
                           CourseScheduleRepository courseScheduleRepository,
                           ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.courseScheduleRepository = courseScheduleRepository;
        this.reviewRepository = reviewRepository;
    }
    
    @Override
    public IntentType getSupportedIntentType() {
        return IntentType.SUPPLEMENT;
    }
    
    @Override
    public AgentResult process(IntentResult intent, ConversationContext context) {
        logger.info("[SupplementAgent] 处理条件补充: {}", intent.getOriginalMessage());
        
        // 合并新旧参数
        Map<String, Object> mergedParams = new HashMap<>(context.getParameters());
        mergedParams.putAll(intent.getParameters());
        
        logger.info("[SupplementAgent] 合并后的参数: {}", mergedParams);
        
        // 获取所有课程并过滤
        List<Course> allCourses = courseRepository.findAll();
        List<Course> filteredCourses = filterCourses(allCourses, mergedParams, intent.getKeywords());
        
        logger.info("[SupplementAgent] 过滤后课程数量: {}", filteredCourses.size());
        
        if (filteredCourses.isEmpty()) {
            // 提供具体的条件说明
            StringBuilder message = new StringBuilder();
            message.append("抱歉，根据您更新后的条件：\n");
            
            if (mergedParams.containsKey("faculty")) {
                message.append("- 学院：").append(mergedParams.get("faculty")).append("\n");
            }
            if (mergedParams.containsKey("courseType")) {
                String type = (String) mergedParams.get("courseType");
                message.append("- 类型：").append("COMPULSORY".equals(type) ? "必修课" : "选修课").append("\n");
            }
            if (mergedParams.containsKey("credits")) {
                message.append("- 学分：").append(mergedParams.get("credits")).append("\n");
            }
            if (mergedParams.containsKey("dayOfWeek")) {
                String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                Object dayOfWeekObj = mergedParams.get("dayOfWeek");
                if (dayOfWeekObj instanceof List) {
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
                        message.append("- 上课日：").append(dayStr).append("\n");
                    }
                } else if (dayOfWeekObj instanceof Number) {
                    int day = ((Number) dayOfWeekObj).intValue();
                    if (day >= 1 && day <= 7) {
                        message.append("- 上课日：").append(dayNames[day]).append("\n");
                    }
                }
            }
            if (mergedParams.containsKey("timePeriod")) {
                int period = (int) mergedParams.get("timePeriod");
                String[] periodNames = {"", "上午(09:00-11:50)", "中午(12:30-15:20)", "下午(15:30-18:20)", "晚上(19:00-21:50)"};
                message.append("- 时间段：").append(periodNames[period]).append("\n");
            }
            
            message.append("\n暂时没有找到匹配的课程。您可以尝试放宽一些条件。");
            
            return AgentResult.text(IntentType.SUPPLEMENT, message.toString());
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
        Map<String, Object> updatedContext = new HashMap<>(mergedParams);
        updatedContext.put("lastIntentType", IntentType.SUPPLEMENT.name());
        
        return AgentResult.builder()
                .intentType(IntentType.SUPPLEMENT)
                .success(true)
                .courses(courseInfos)
                .updatedContext(updatedContext)
                .build();
    }
    
    private List<Course> filterCourses(List<Course> courses, Map<String, Object> params, List<String> keywords) {
        // 预先获取符合时间条件的课程ID集合
        Set<Long> timeMatchedCourseIds = getTimeMatchedCourseIds(params);
        
        return courses.stream()
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
                    logger.info("[SupplementAgent] 时间过滤: 周{} 时间段{}, 找到 {} 个时间安排", 
                            dayOfWeek, timePeriod, schedules.size());
                } else {
                    schedules = courseScheduleRepository.findByDayOfWeek(dayOfWeek);
                    logger.info("[SupplementAgent] 时间过滤: 周{}, 找到 {} 个时间安排", dayOfWeek, schedules.size());
                }
                schedules.forEach(s -> matchedCourseIds.add(s.getCourse().getId()));
            }
        } else if (timePeriod != null) {
            // 只指定时间段
            List<CourseSchedule> schedules = courseScheduleRepository.findByTimePeriod(timePeriod);
            logger.info("[SupplementAgent] 时间过滤: 时间段{}, 找到 {} 个时间安排", timePeriod, schedules.size());
            schedules.forEach(s -> matchedCourseIds.add(s.getCourse().getId()));
        }
        
        logger.info("[SupplementAgent] 时间过滤后匹配的课程ID数量: {}", matchedCourseIds.size());
        return matchedCourseIds;
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

