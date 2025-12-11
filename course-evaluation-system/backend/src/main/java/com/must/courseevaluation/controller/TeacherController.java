package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.TeacherDto;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/teachers")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @GetMapping
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        List<TeacherDto> teachers = teacherService.findAll();
        return ResponseEntity.ok(teachers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TeacherDto> getTeacherById(@PathVariable Long id) {
        Teacher teacher = teacherService.findById(id);
        return ResponseEntity.ok(TeacherDto.fromEntity(teacher));
    }
    
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<TeacherDto>> getTeachersByFaculty(@PathVariable Long facultyId) {
        List<TeacherDto> teachers = teacherService.findByFacultyId(facultyId);
        return ResponseEntity.ok(teachers);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TeacherDto> createTeacher(@Valid @RequestBody TeacherDto teacherDto) {
        TeacherDto createdTeacher = teacherService.create(teacherDto);
        return new ResponseEntity<>(createdTeacher, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TeacherDto> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherDto teacherDto) {
        TeacherDto updatedTeacher = teacherService.update(id, teacherDto);
        return ResponseEntity.ok(updatedTeacher);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 