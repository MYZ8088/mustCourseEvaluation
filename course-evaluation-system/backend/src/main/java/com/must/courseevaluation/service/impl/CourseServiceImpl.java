package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.CourseScheduleDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.CourseSchedule;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.model.UserSchedule;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.CourseScheduleRepository;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.TeacherRepository;
import com.must.courseevaluation.repository.UserScheduleRepository;
import com.must.courseevaluation.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private CourseScheduleRepository courseScheduleRepository;
    
    @Autowired
    private UserScheduleRepository userScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        // 使用简化查询获取课程（包含院系和教师）
        List<Course> courses = courseRepository.findAllWithRelations();
        return courses.stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        // 使用优化查询获取课程及关联数据
        Course course = courseRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + id));
        return CourseDto.fromEntity(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("院系不存在，ID: " + facultyId);
        }
        List<Course> courses = courseRepository.findByFacultyIdWithRelations(facultyId);
        return courses.stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("教师不存在，ID: " + teacherId);
        }
        List<Course> courses = courseRepository.findByTeacherIdWithRelations(teacherId);
        return courses.stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByType(Course.CourseType type) {
        return courseRepository.findByType(type).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> searchCourses(String keyword) {
        return courseRepository.findByNameContaining(keyword).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        // 检查课程代码是否已存在
        if (courseRepository.existsByCode(courseDto.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + courseDto.getCode());
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(courseDto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在，ID: " + courseDto.getFacultyId()));

        // 构建课程实体
        Course course = new Course();
        course.setCode(courseDto.getCode());
        course.setName(courseDto.getName());
        course.setCredits(courseDto.getCredits());
        course.setDescription(courseDto.getDescription());
        course.setType(Course.CourseType.valueOf(courseDto.getType()));
        course.setAssessmentCriteria(courseDto.getAssessmentCriteria());
        course.setFaculty(faculty);

        // 如果指定了教师，设置教师
        if (courseDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("教师不存在，ID: " + courseDto.getTeacherId()));
            course.setTeacher(teacher);
        }

        // 保存课程
        Course savedCourse = courseRepository.save(course);
        return CourseDto.fromEntity(savedCourse);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(CourseDto courseDto) {
        // 检查课程是否存在
        Course existingCourse = courseRepository.findById(courseDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseDto.getId()));

        // 如果更改了课程代码，检查新代码是否已存在
        if (!existingCourse.getCode().equals(courseDto.getCode()) && courseRepository.existsByCode(courseDto.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + courseDto.getCode());
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(courseDto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在，ID: " + courseDto.getFacultyId()));

        // 更新课程信息
        existingCourse.setCode(courseDto.getCode());
        existingCourse.setName(courseDto.getName());
        existingCourse.setCredits(courseDto.getCredits());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setType(Course.CourseType.valueOf(courseDto.getType()));
        existingCourse.setAssessmentCriteria(courseDto.getAssessmentCriteria());
        existingCourse.setFaculty(faculty);

        // 更新教师信息
        if (courseDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("教师不存在，ID: " + courseDto.getTeacherId()));
            existingCourse.setTeacher(teacher);
        } else {
            existingCourse.setTeacher(null);
        }

        // 保存更新后的课程
        Course updatedCourse = courseRepository.save(existingCourse);
        return CourseDto.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("课程不存在，ID: " + id);
        }
        courseRepository.deleteById(id);
    }
    
    // ==================== 课程时间表相关方法 ====================
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseScheduleDto> getCourseSchedules(Long courseId) {
        return courseScheduleRepository.findByCourseId(courseId).stream()
                .map(CourseScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CourseScheduleDto addCourseSchedule(Long courseId, CourseScheduleDto scheduleDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 检查是否已存在相同时间的安排
        if (courseScheduleRepository.existsByCourseIdAndDayOfWeekAndTimePeriod(
                courseId, scheduleDto.getDayOfWeek(), scheduleDto.getTimePeriod())) {
            throw new IllegalArgumentException("该时间段已有课程安排");
        }
        
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourse(course);
        schedule.setDayOfWeek(scheduleDto.getDayOfWeek());
        schedule.setTimePeriod(scheduleDto.getTimePeriod());
        schedule.setLocation(scheduleDto.getLocation());
        
        CourseSchedule savedSchedule = courseScheduleRepository.save(schedule);
        return CourseScheduleDto.fromEntity(savedSchedule);
    }
    
    @Override
    @Transactional
    public CourseScheduleDto updateCourseSchedule(Long scheduleId, CourseScheduleDto scheduleDto) {
        CourseSchedule schedule = courseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("课程时间安排不存在，ID: " + scheduleId));
        
        // 如果时间改变，检查是否冲突
        if (!schedule.getDayOfWeek().equals(scheduleDto.getDayOfWeek()) ||
            !schedule.getTimePeriod().equals(scheduleDto.getTimePeriod())) {
            if (courseScheduleRepository.existsByCourseIdAndDayOfWeekAndTimePeriod(
                    schedule.getCourse().getId(), scheduleDto.getDayOfWeek(), scheduleDto.getTimePeriod())) {
                throw new IllegalArgumentException("该时间段已有课程安排");
            }
        }
        
        schedule.setDayOfWeek(scheduleDto.getDayOfWeek());
        schedule.setTimePeriod(scheduleDto.getTimePeriod());
        schedule.setLocation(scheduleDto.getLocation());
        
        CourseSchedule updatedSchedule = courseScheduleRepository.save(schedule);
        return CourseScheduleDto.fromEntity(updatedSchedule);
    }
    
    @Override
    @Transactional
    public void deleteCourseSchedule(Long scheduleId) {
        if (!courseScheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("课程时间安排不存在，ID: " + scheduleId);
        }
        courseScheduleRepository.deleteById(scheduleId);
    }
    
    @Override
    @Transactional
    public List<CourseScheduleDto> setCourseSchedules(Long courseId, List<CourseScheduleDto> scheduleDtos) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 删除原有的所有时间安排
        courseScheduleRepository.deleteByCourseId(courseId);
        
        // 添加新的时间安排
        List<CourseScheduleDto> savedSchedules = new ArrayList<>();
        for (CourseScheduleDto dto : scheduleDtos) {
            CourseSchedule schedule = new CourseSchedule();
            schedule.setCourse(course);
            schedule.setDayOfWeek(dto.getDayOfWeek());
            schedule.setTimePeriod(dto.getTimePeriod());
            schedule.setLocation(dto.getLocation());
            
            CourseSchedule savedSchedule = courseScheduleRepository.save(schedule);
            savedSchedules.add(CourseScheduleDto.fromEntity(savedSchedule));
        }
        
        return savedSchedules;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> findCoursesBySchedule(Integer dayOfWeek, Integer timePeriod) {
        List<CourseSchedule> schedules = courseScheduleRepository.findByDayOfWeekAndTimePeriod(dayOfWeek, timePeriod);
        return schedules.stream()
                .map(schedule -> CourseDto.fromEntity(schedule.getCourse()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> findCoursesWithoutConflict(Long userId) {
        // 获取用户的所有课程时间安排
        List<UserSchedule> userSchedules = userScheduleRepository.findByUserId(userId);
        
        // 构建用户已占用的时间槽集合
        Set<String> occupiedSlots = new HashSet<>();
        for (UserSchedule schedule : userSchedules) {
            occupiedSlots.add(schedule.getDayOfWeek() + "-" + schedule.getTimePeriod());
        }
        
        // 获取所有课程并过滤掉有冲突的
        List<Course> allCourses = courseRepository.findAllWithRelations();
        List<CourseDto> nonConflictingCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            List<CourseSchedule> courseSchedules = courseScheduleRepository.findByCourseId(course.getId());
            boolean hasConflict = false;
            
            for (CourseSchedule schedule : courseSchedules) {
                String slot = schedule.getDayOfWeek() + "-" + schedule.getTimePeriod();
                if (occupiedSlots.contains(slot)) {
                    hasConflict = true;
                    break;
                }
            }
            
            if (!hasConflict) {
                CourseDto dto = CourseDto.fromEntity(course);
                // 填充课程时间安排
                dto.setSchedules(courseSchedules.stream()
                        .map(CourseScheduleDto::fromEntity)
                        .collect(Collectors.toList()));
                nonConflictingCourses.add(dto);
            }
        }
        
        return nonConflictingCourses;
    }
} 