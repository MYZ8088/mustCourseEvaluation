package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.FacultyDto;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Override
    public FacultyDto create(FacultyDto facultyDto) {
        // 检查是否已存在同名学院
        if (facultyRepository.findByName(facultyDto.getName()).isPresent()) {
            throw new RuntimeException("已存在同名学院: " + facultyDto.getName());
        }

        Faculty faculty = new Faculty();
        faculty.setName(facultyDto.getName());
        faculty.setDescription(facultyDto.getDescription());

        Faculty savedFaculty = facultyRepository.save(faculty);
        return FacultyDto.fromEntity(savedFaculty);
    }

    @Override
    public Faculty findById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学院不存在，ID: " + id));
    }
    
    @Override
    public FacultyDto getById(Long id) {
        Faculty faculty = findById(id);
        return FacultyDto.fromEntity(faculty);
    }

    @Override
    public List<FacultyDto> findAll() {
        return facultyRepository.findAll().stream()
                .map(FacultyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public FacultyDto update(Long id, FacultyDto facultyDto) {
        Faculty faculty = findById(id);

        // 如果名称更改，需检查是否已存在同名学院
        if (!faculty.getName().equals(facultyDto.getName()) && 
                facultyRepository.findByName(facultyDto.getName()).isPresent()) {
            throw new RuntimeException("已存在同名学院: " + facultyDto.getName());
        }

        faculty.setName(facultyDto.getName());
        faculty.setDescription(facultyDto.getDescription());

        Faculty updatedFaculty = facultyRepository.save(faculty);
        return FacultyDto.fromEntity(updatedFaculty);
    }

    @Override
    public void delete(Long id) {
        Faculty faculty = findById(id);
        
        // 检查学院下是否有课程
        if (!faculty.getCourses().isEmpty()) {
            throw new RuntimeException("不能删除包含课程的学院");
        }
        
        facultyRepository.delete(faculty);
    }
} 