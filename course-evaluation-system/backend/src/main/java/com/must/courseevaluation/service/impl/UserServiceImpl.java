package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查电子邮件是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("电子邮件已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setStudentId(registerRequest.getStudentId());
        user.setFullName(registerRequest.getFullName());
        user.setRole(User.Role.ROLE_STUDENT);

        // 保存用户
        User savedUser = userRepository.save(user);

        // 返回DTO
        return UserDto.fromEntity(savedUser);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = findById(id);
        boolean statusChanged = false;
        boolean commentStatusChanged = false;
        
        if (userDto.isActive() != user.isActive()) {
            logger.info("用户状态更改 - 用户ID: {}, 用户名: {}, 旧状态: {}, 新状态: {}", 
                id, user.getUsername(), user.isActive() ? "启用" : "停用", userDto.isActive() ? "启用" : "停用");
            statusChanged = true;
        }
        
        if (userDto.isCanComment() != user.isCanComment()) {
            logger.info("用户评论权限更改 - 用户ID: {}, 用户名: {}, 旧状态: {}, 新状态: {}", 
                id, user.getUsername(), user.isCanComment() ? "允许评论" : "禁言", userDto.isCanComment() ? "允许评论" : "禁言");
            commentStatusChanged = true;
        }

        // 更新用户信息
        if (userDto.getUsername() != null && !user.getUsername().equals(userDto.getUsername())) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("用户名已被使用");
            }
            logger.info("用户名更改 - 用户ID: {}, 旧用户名: {}, 新用户名: {}", id, user.getUsername(), userDto.getUsername());
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getEmail() != null && !user.getEmail().equals(userDto.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("电子邮件已被使用");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getStudentId() != null) {
            user.setStudentId(userDto.getStudentId());
        }

        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
        }

        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }
        
        // 更新密码，如果提供了新密码
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            logger.info("用户密码更改 - 用户ID: {}, 用户名: {}", id, user.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        // 更新active状态（账号启用/停用）
        user.setActive(userDto.isActive());
        
        // 更新canComment状态（禁言/允许评论）
        user.setCanComment(userDto.isCanComment());

        // 保存更新后的用户
        User updatedUser = userRepository.save(user);
        
        if (statusChanged || commentStatusChanged) {
            logger.info("用户状态更新成功 - 用户ID: {}, 用户名: {}", id, updatedUser.getUsername());
        }

        // 返回DTO
        return UserDto.fromEntity(updatedUser);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在，ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 