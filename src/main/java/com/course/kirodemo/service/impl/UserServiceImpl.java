package com.course.kirodemo.service.impl;

import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.UserAlreadyExistsException;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserService 實作類別
 * 處理使用者相關的業務邏輯，包含註冊、認證和密碼管理
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    @Override
    public User registerUser(UserRegistrationRequest registrationRequest) {
        // 驗證註冊請求
        if (registrationRequest == null || !registrationRequest.isValid()) {
            throw new IllegalArgumentException("註冊資料無效");
        }
        
        logger.info("嘗試註冊使用者: {}", registrationRequest.getUsername());
        
        // 檢查使用者名稱是否已存在
        if (existsByUsername(registrationRequest.getUsername())) {
            logger.warn("註冊失敗，使用者名稱已存在: {}", registrationRequest.getUsername());
            throw UserAlreadyExistsException.forUsername(registrationRequest.getUsername());
        }
        
        // 加密密碼
        String encodedPassword = encodePassword(registrationRequest.getPassword());
        
        // 建立使用者實體
        User user = registrationRequest.toEntity(encodedPassword);
        
        // 儲存使用者
        User savedUser = userRepository.save(user);
        logger.info("使用者註冊成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return userRepository.findByUsername(username.trim());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return false;
        }
        
        Optional<User> userOptional = findByUsername(username);
        if (userOptional.isEmpty()) {
            logger.warn("登入驗證失敗，使用者不存在: {}", username);
            return false;
        }
        
        User user = userOptional.get();
        boolean isValid = passwordEncoder.matches(rawPassword, user.getPassword());
        
        if (isValid) {
            logger.info("使用者登入驗證成功: {}", username);
        } else {
            logger.warn("使用者登入驗證失敗，密碼錯誤: {}", username);
        }
        
        return isValid;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.existsByUsername(username.trim());
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密碼不能為空");
        }
        
        return passwordEncoder.encode(rawPassword);
    }
}