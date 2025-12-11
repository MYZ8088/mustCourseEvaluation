package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.FacultyDto;
import com.must.courseevaluation.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/faculties")
public class FacultyController {
    
    @Autowired
    private FacultyService facultyService;
    
    @GetMapping
    public ResponseEntity<List<FacultyDto>> getAllFaculties() {
        List<FacultyDto> faculties = facultyService.findAll();
        return ResponseEntity.ok(faculties);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FacultyDto> getFacultyById(@PathVariable Long id) {
        FacultyDto faculty = facultyService.getById(id);
        return ResponseEntity.ok(faculty);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FacultyDto> createFaculty(@Valid @RequestBody FacultyDto facultyDto) {
        FacultyDto createdFaculty = facultyService.create(facultyDto);
        return new ResponseEntity<>(createdFaculty, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FacultyDto> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyDto facultyDto) {
        FacultyDto updatedFaculty = facultyService.update(id, facultyDto);
        return ResponseEntity.ok(updatedFaculty);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 