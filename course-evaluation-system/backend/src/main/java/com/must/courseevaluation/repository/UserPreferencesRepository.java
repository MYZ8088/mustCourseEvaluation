package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    /**
     * 根据用户ID查找偏好
     */
    Optional<UserPreferences> findByUserId(Long userId);
}




















