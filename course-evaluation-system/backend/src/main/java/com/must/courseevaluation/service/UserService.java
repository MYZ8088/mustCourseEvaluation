package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;

import java.util.List;

public interface UserService {
    UserDto register(RegisterRequest registerRequest);
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
    List<UserDto> findAll();
    UserDto update(Long id, UserDto userDto);
    void delete(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 