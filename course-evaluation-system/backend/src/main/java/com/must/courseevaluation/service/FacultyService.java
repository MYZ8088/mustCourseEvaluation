package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.FacultyDto;
import com.must.courseevaluation.model.Faculty;

import java.util.List;

public interface FacultyService {
    FacultyDto create(FacultyDto facultyDto);
    Faculty findById(Long id);
    FacultyDto getById(Long id);
    List<FacultyDto> findAll();
    FacultyDto update(Long id, FacultyDto facultyDto);
    void delete(Long id);
} 