package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.TeacherDto;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.TeacherRepository;
import com.must.courseevaluation.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, FacultyRepository facultyRepository) {
        this.teacherRepository = teacherRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public TeacherDto create(TeacherDto teacherDto) {
        // 检查教师名称是否已存在
        if (teacherRepository.existsByName(teacherDto.getName())) {
            throw new RuntimeException("教师名称已存在");
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(teacherDto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("未找到指定的院系"));

        // 创建新教师
        Teacher teacher = new Teacher();
        teacher.setName(teacherDto.getName());
        teacher.setTitle(teacherDto.getTitle());
        teacher.setEmail(teacherDto.getEmail());
        teacher.setResearchField(teacherDto.getResearchField());
        teacher.setAchievements(teacherDto.getAchievements());
        teacher.setFaculty(faculty);

        // 保存教师
        Teacher savedTeacher = teacherRepository.save(teacher);

        // 返回DTO
        return TeacherDto.fromEntity(savedTeacher);
    }

    @Override
    @Transactional(readOnly = true)
    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到教师，ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> findAll() {
        // 使用简单的 findAll()，faculty 是 EAGER 加载会自动获取
        List<Teacher> teachers = teacherRepository.findAll();
        
        if (teachers.isEmpty()) {
            return List.of();
        }
        
        // 获取课程数量映射（单独查询避免复杂 JOIN）
        List<Object[]> courseCounts = teacherRepository.findTeacherCourseCounts();
        java.util.Map<Long, Long> courseCountMap = new java.util.HashMap<>();
        for (Object[] row : courseCounts) {
            Long teacherId = (Long) row[0];
            Long count = (Long) row[1];
            if (teacherId != null) {
                courseCountMap.put(teacherId, count);
            }
        }
        
        // 转换为DTO
        return teachers.stream()
                .map(teacher -> {
                    TeacherDto dto = TeacherDto.fromEntity(teacher);
                    Long count = courseCountMap.getOrDefault(teacher.getId(), 0L);
                    dto.setCourseCount(count.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> findByFacultyId(Long facultyId) {
        return teacherRepository.findByFacultyId(facultyId).stream()
                .map(TeacherDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDto update(Long id, TeacherDto teacherDto) {
        Teacher teacher = findById(id);

        // 如果名称已更改，检查是否与现有名称冲突
        if (teacherDto.getName() != null && !teacher.getName().equals(teacherDto.getName())) {
            if (teacherRepository.existsByName(teacherDto.getName())) {
                throw new RuntimeException("教师名称已存在");
            }
            teacher.setName(teacherDto.getName());
        }

        // 更新其他字段
        if (teacherDto.getTitle() != null) {
            teacher.setTitle(teacherDto.getTitle());
        }
        
        if (teacherDto.getEmail() != null) {
            teacher.setEmail(teacherDto.getEmail());
        }
        
        if (teacherDto.getResearchField() != null) {
            teacher.setResearchField(teacherDto.getResearchField());
        }
        
        if (teacherDto.getAchievements() != null) {
            teacher.setAchievements(teacherDto.getAchievements());
        }

        // 更新院系
        if (teacherDto.getFacultyId() != null && (teacher.getFaculty() == null || !teacher.getFaculty().getId().equals(teacherDto.getFacultyId()))) {
            Faculty faculty = facultyRepository.findById(teacherDto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("未找到指定的院系"));
            teacher.setFaculty(faculty);
        }

        // 保存更新
        Teacher updatedTeacher = teacherRepository.save(teacher);

        // 返回DTO
        return TeacherDto.fromEntity(updatedTeacher);
    }

    @Override
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("未找到教师，ID: " + id);
        }
        teacherRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return teacherRepository.existsByName(name);
    }
} 