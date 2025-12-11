package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.TeacherDto;
import com.must.courseevaluation.model.Teacher;

import java.util.List;

public interface TeacherService {
    TeacherDto create(TeacherDto teacherDto);
    Teacher findById(Long id);
    List<TeacherDto> findAll();
    List<TeacherDto> findByFacultyId(Long facultyId);
    TeacherDto update(Long id, TeacherDto teacherDto);
    void delete(Long id);
    boolean existsByName(String name);
} 